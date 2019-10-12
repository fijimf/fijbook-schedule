package com.fijimf.deepfi.schedule.services

import java.time.LocalDateTime

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import cats.kernel.Eq
import com.fijimf.deepfi.schedule.model._
import doobie.implicits._
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor

final case class Updater[F[_]](xa: Transactor[F])(implicit F: Sync[F]) {
  implicit val eqGame: Eq[Game] = Eq.fromUniversalEquals
  implicit val eqResult: Eq[Result] = Eq.fromUniversalEquals
  /** Update games and results is a little subtle
   * 1) A 'loadKey' is the key used by the scraping model to break up the updates.  Typically it is by date, but scraping date is not the same as game date -- Hawaii night games.
   * 2) For a given loadKey we want to
   *    - insert any games which are missing
   *    - update any games which already exist and are different
   *    - delete any games which are not in the set of proposed games
   *
   * 3) We can't simply do delete/insert because we need game_id to be stable.
   *
   */
  def updateGamesAndResults(updates: List[UpdateCandidate], loadKey: String): F[List[(Game, Option[Result])]] = {
    for {
      updatesWithKeys <- findKeys(updates, loadKey)
      gamesWithKeys <- loadGamesAndResults(loadKey)
      mods <- doUpdates(updatesWithKeys, gamesWithKeys, loadKey)
    } yield {
      mods
    }
  }

  def findDeletes(updatesWithKeys: List[(GameKey, UpdateCandidate)], gamesWithKeys: Map[GameKey, (Game, Option[Result])]): List[Game] = {
    val updateKeys: List[GameKey] = updatesWithKeys.map(_._1)
    gamesWithKeys
      .filter { case (k, _) => updateKeys.contains(k) }
      .values
      .map(_._1)
      .toList
  }

  def doUpdates(updatesWithKeys: Map[GameKey, (Game, Option[Result])], gamesWithKeys: Map[GameKey, (Game, Option[Result])], loadKey: String): F[List[(Game, Option[Result])]] = {
    val keys: Set[GameKey] = updatesWithKeys.keySet ++ gamesWithKeys.keySet

    keys.toList.map(k => {
      (gamesWithKeys.get(k), updatesWithKeys.get(k)) match {
        case (Some((g, Some(r))), Some((h, Some(s)))) =>
          val h1: Game = h.copy(id = g.id)
          val s1: Result = s.copy(id = r.id, gameId = r.gameId)
          if (g === h1 && r === s1) {
            F.pure(List.empty[(Game, Option[Result])])
          } else {
            update(h1, Some(s1))
          }
        case (Some((g, None)), Some((h, Some(s)))) =>
          update(h.copy(id = g.id), Some(s))
        case (Some((g, Some(r))), Some((h, None))) =>
          update(h.copy(id = g.id), Some(r.copy(id = -r.id)))
        case (Some((g, None)), Some((h, None))) =>
          val h1: Game = h.copy(id = g.id)
          if (h1 === g) {
            F.pure(List.empty[(Game, Option[Result])])
          } else {
            update(h1, None)
          }
        case (Some((g, or)), None) => // No game or result in update pure delete
          update(g.copy(id = -g.id), or.map(r => r.copy(id = -r.id)))

        case (None, Some((g, or))) => // No existing game or result pure insert
          update(g, or)

        case (None, None) => //cant happen
          F.pure(List.empty[(Game, Option[Result])])
      }
    }).sequence.map(_.flatten)
  }

  def update(g: Game, or: Option[Result]): F[List[(Game, Option[Result])]] = {
    for {
      game <- updateGame(g)
      optionResult <- updateOptionResult(or.map(_.copy(gameId = game.id)))
    } yield {
      if (game.id < 0) {
        List.empty[(Game, Option[Result])]
      } else {
        List((game, optionResult))
      }
    }
  }

  def updateGame(g: Game): F[Game] = {
    import Game.Dao._
    if (g.id < 0) {
      Game.Dao.delete(-g.id).run.transact(xa).map(_ => g)
    } else if (g.id === 0) {
      Game.Dao.insert(g).withUniqueGeneratedKeys[Game](Game.Dao.cols: _*).transact(xa)
    } else {
      Game.Dao.update(g).withUniqueGeneratedKeys[Game](Game.Dao.cols: _*).transact(xa)
    }
  }

  def updateOptionResult(optionResult: Option[Result]): F[Option[Result]] = {
    optionResult match {
      case Some(result) =>
        if (result.id < 0) {
          Result.Dao.delete(-result.id).run.transact(xa).map(_ => None)
        } else if (result.id === 0) {
          Result.Dao.insert(result).withUniqueGeneratedKeys[Result](Result.Dao.cols: _*).transact(xa).map(Option(_))
        } else {
          Result.Dao.update(result).withUniqueGeneratedKeys[Result](Result.Dao.cols: _*).transact(xa).map(Option(_))
        }
      case None => F.pure(None)
    }
  }

  def findKeys(pgs: List[UpdateCandidate], loadKey: String): F[Map[GameKey, (Game, Option[Result])]] = {
    pgs.map(findKeysForGame(_, loadKey)).sequence.map(_.flatten.toMap)
  }

  def findKeysForGame(pg: UpdateCandidate, loadKey: String): F[List[(GameKey, (Game, Option[Result]))]] = {
    (for {
      ht <- loadTeam(pg.homeKey)
      at <- loadTeam(pg.awayKey)
      s <- findSeason(pg.dateTime)
    } yield {
      val key = GameKey(pg.date.toEpochDay, s.id, ht.id, at.id)
      key -> (pg.toGame(0L, key, loadKey), pg.toOptionResult(0L, 0L))
    }).value.map(_.toList)
  }

  def loadGamesAndResults(loadKey: String): F[Map[GameKey, (Game, Option[Result])]] = {
    for {
      gs <- Game.Dao.findByLoadKey(loadKey).to[List].transact(xa)
    } yield {
      gs.map(t => GameKey(t._1.date.toEpochDay, t._1.homeTeamId, t._1.awayTeamId, t._1.seasonId) -> t).toMap
    }
  }

  def loadTeam(key: String): OptionT[F, Team] = {
    val a: F[Option[Team]] = Team.Dao.findByKey(key).option.transact(xa)
    val b: F[Option[Team]] = Team.Dao.findByAlias(key).option.transact(xa)

    OptionT[F,Team](a).orElseF(b)




  }

  def findSeason(date: LocalDateTime): OptionT[F, Season] = OptionT(for {
    s <- Season.Dao.findByDate(date).option.transact(xa)
  } yield {
    s
  })

}
