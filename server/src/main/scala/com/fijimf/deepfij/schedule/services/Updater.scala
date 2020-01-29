package com.fijimf.deepfij.schedule.services

import java.time.LocalDateTime

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import cats.kernel.Eq
import com.fijimf.deepfij.schedule.model.Dao.{GameDao, ResultDao, SeasonDao, TeamDao}
import com.fijimf.deepfij.schedule.model._
import com.fijimf.deepfij.schedule.model.Game
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.slf4j.{Logger, LoggerFactory}

final case class Updater[F[_]](xa: Transactor[F])(implicit F: Sync[F]) {
  val log: Logger =LoggerFactory.getLogger(Updater.getClass)
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
      _ <- F.delay(log.info(s"For $loadKey, update candidates generated ${updatesWithKeys.size} games to update."))
      gamesWithKeys <- loadGamesAndResults(loadKey)
      _ <- F.delay(log.info(s"For $loadKey, ${gamesWithKeys.size} existing games were loaded."))
      mods <- doUpdates(updatesWithKeys, gamesWithKeys, loadKey)
      _ <- F.delay(log.info(s"For $loadKey, ${mods.size} changes were made."))
    } yield {
      mods
    }
  }


  def doUpdates(updatesWithKeys: Map[GameKey, (Game, Option[Result])], gamesWithKeys: Map[GameKey, (Game, Option[Result])], loadKey: String): F[List[(Game, Option[Result])]] = {
    val updateKeys: Set[GameKey] = updatesWithKeys.keySet
    val existingKeys: Set[GameKey] = gamesWithKeys.keySet

    val inserts: Set[GameKey] = updateKeys.diff(existingKeys)
    val deletes: Set[GameKey] = existingKeys.diff(updateKeys)
    val updates: Set[GameKey] = updateKeys.intersect(existingKeys)
    log.info(s"${inserts.size} inserts, ${deletes.size} deletes, ${updates.size} updates ")
    inserts.foreach(println(_))
    for {
      is <- inserts.flatMap(k => updatesWithKeys.get(k)).map((insert _).tupled).toList.sequence
      ds <- deletes.flatMap(k => gamesWithKeys.get(k)).map((delete _).tupled).toList.sequence
      us <- (for {
        k <- updates
        n <- updatesWithKeys.get(k)
        o <- gamesWithKeys.get(k) if updateNeeded(o,n)
      } yield {
        update(o._1, n._1, o._2, n._2)
      }).toList.sequence
    } yield {
      is ++ ds ++ us
    }
  }

  def updateNeeded(oldPair: (Game, Option[Result]), newPair: (Game, Option[Result])): Boolean = {
    val (oldGame, oldRes) = oldPair
    val (newGame, newRes) = newPair
    val augmentedGame: Game = newGame.copy(id = oldGame.id)
    val augmentedRes: Option[Result] = newRes.map(_.copy(
      id = oldRes.map(_.id).getOrElse(0),
      gameId = oldRes.map(_.gameId).getOrElse(0)
    ))
    !(oldGame === augmentedGame && oldRes === augmentedRes)
  }


  def insert(game: Game, res: Option[Result]): F[(Game, Option[Result])] = {
    import GameDao._
    for {
      g: Game <- GameDao.insert(game).withUniqueGeneratedKeys[Game](GameDao.cols: _ *).transact(xa)
      o: Option[Result] <- res match {
        case Some(r) =>
          ResultDao.insert(r.copy(gameId = g.id))
            .withUniqueGeneratedKeys[Result](ResultDao.cols: _*)
            .transact(xa)
            .map(Option(_))
        case None =>
          F.pure(Option.empty[Result])
      }
    } yield {
      (g, o)
    }
  }

  def delete(game: Game, res: Option[Result]): F[(Game, Option[Result])] = {
    for {
      _ <- ResultDao.deleteByGameId(game.id).run.transact(xa)
      _ <- GameDao.delete(game.id).run.transact(xa)
    } yield {
      (game, res)
      }
  }

  def update(g: Game, h: Game, o: Option[Result], p: Option[Result]): F[(Game, Option[Result])] = {
    for {
      gg <- gameUpdate(g, h.copy(id = g.id))
      oo <- optResultUpdate(gg.id, o, p)
    } yield {
      (gg, oo)
    }
  }

  private def gameUpdate(g: Game, h1: Game): F[Game] = {
    import GameDao._
    for {
      h2 <- GameDao.update(h1).withUniqueGeneratedKeys[Game](GameDao.cols: _ *).transact(xa)
    } yield {
      h2
    }
  }

  private def optResultUpdate(gameId:Long, o: Option[Result], p: Option[Result]): F[Option[Result]] = {
    (o, p) match {
      case (Some(o1), Some(p1)) =>
        ResultDao.update(p1.copy(id = o1.id, gameId = o1.gameId)).withUniqueGeneratedKeys[Result](ResultDao.cols: _ *).transact(xa).map(Some(_))
      case (Some(o1), None) =>
        ResultDao.delete(o1.id).run.transact(xa).map(_ => Option.empty[Result])
      case (None, Some(p1)) =>
        ResultDao.insert(p1.copy(gameId=gameId)).withUniqueGeneratedKeys[Result](ResultDao.cols: _ *).transact(xa).map(Some(_))
      case (None, None) => F.pure(None)
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
      key = GameKey(pg.date.toEpochDay, s.id, ht.id, at.id)
    } yield {
      log.info(s"$pg => $key")
      key -> (pg.toGame(0L, key, loadKey), pg.toOptionResult(0L, 0L))
    }).value.map(_.toList)
  }

  def loadGamesAndResults(loadKey: String): F[Map[GameKey, (Game, Option[Result])]] = {
    for {
      gs <- GameDao.findByLoadKey(loadKey).to[List].transact(xa)
    } yield {
      gs.map(t => {
        val key: GameKey = GameKey(t._1.date.toEpochDay, t._1.seasonId, t._1.homeTeamId, t._1.awayTeamId)
        log.info(s"=> $key")
        key -> t
      }).toMap
    }
  }

  def loadTeam(key: String): OptionT[F, Team] = {
    OptionT[F,Team](TeamDao.findByKey(key).option.transact(xa))
      .orElseF(TeamDao.findByAlias(key).option.transact(xa))
  }

  def findSeason(date: LocalDateTime): OptionT[F, Season] = OptionT(for {
    s <- SeasonDao.findByDate(date).option.transact(xa)
  } yield {
    s
  })

}
