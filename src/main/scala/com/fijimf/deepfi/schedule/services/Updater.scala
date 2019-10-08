package com.fijimf.deepfi.schedule.services

import java.time.LocalDateTime

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import cats.kernel.Eq
import com.fijimf.deepfi.schedule.model._
import doobie.implicits._
import doobie.util.transactor.Transactor

class Updater[F[_]](xa: Transactor[F])(implicit F: Sync[F]) {

  case class MatchKey(day: Long, seasonId: Long, homeTeamId: Long, awayTeamId: Long)

  implicit val eqGame: Eq[Game] = Eq.fromUniversalEquals
  /** Update games and results is a little subtle
   * 1) A 'loadKey' is the key used by the scraping model to break up the updates.  Typically it is by date, but scraping date is not the same as game date -- Hawaii night games.
   * 2) For a given loadKey we want to
   *    - insert any games which are missing
   *    - update any games which already exist and are different
   *    - delete any games which are not in the set of proposed games
   *
   * 3) We can't simply do delete/insert because we need game_id to be stable.
   */
  def updateGamesAndResults(pgs: List[ProposedGame], pgrs: List[ProposedGameResult], loadKey: String): F[(List[Game], List[Game], List[Game])] = {
    for {
      pgList <- findKeys(pgs)
      gMap <- loadGamesAndResults(loadKey)
      deletes = findDeletes(pgList, gMap)
      mods = findMods(pgList, gMap, loadKey)
      _ <- deletes.traverse(d => Game.Dao.delete(d.id).run).transact(xa)
      _ <- mods.traverse(g => if (g.id === 0L) Game.Dao.insert(g).run else Game.Dao.update(g).run).transact(xa)

    } yield {
      (deletes, mods.filter(_.id === 0L), mods.filter(_.id =!= 0L))
    }
  }

  def findDeletes(pgList: List[(MatchKey, ProposedGame)], gMap: Map[MatchKey, (Game, Option[Result])]): List[Game] = {
    val updateKeys: List[MatchKey] = pgList.map(_._1)
    gMap
      .filter { case (k, _) => updateKeys.contains(k) }
      .values
      .map(_._1)
      .toList
  }

  def findMods(pgList: List[(MatchKey, ProposedGame)], gMap: Map[MatchKey, (Game, Option[Result])], loadKey: String): List[Game] = {
    pgList.flatMap { case (k, v) =>
      gMap.get(k) match {
        case Some((g, _)) =>
          val update: Game = v.toGame(g.id, k.seasonId, k.homeTeamId, k.awayTeamId, loadKey)
          if (update === g)
            List.empty[Game]
          else
            List(update)
        case None =>
          val insert: Game = v.toGame(0L, k.seasonId, k.homeTeamId, k.awayTeamId, loadKey)
          List(insert)
      }
    }
  }

  def findKeys(pgs: List[ProposedGame]): F[List[(MatchKey, ProposedGame)]] = {
    pgs.map(findKeysForGame).sequence.map(_.flatten)
  }

  private def findKeysForGame(pg: ProposedGame): F[List[(MatchKey, ProposedGame)]] = {

    (for {
      ht <- loadTeam(pg.homeKey)
      at <- loadTeam(pg.awayKey)
      s <- findSeason(pg.dateTime)
    } yield {
      MatchKey(pg.date.toEpochDay, ht.id, at.id, s.id) -> pg
    }).value.map(_.toList)

  }

  def loadGamesAndResults(loadKey: String): F[Map[MatchKey, (Game, Option[Result])]] = {
    for {
      gs <- Game.Dao.findByLoadKey(loadKey).to[List].transact(xa)
    } yield {
      gs.map(t => MatchKey(t._1.date.toEpochDay, t._1.homeTeamId, t._1.awayTeamId, t._1.seasonId) -> t).toMap
    }
  }

  def loadTeam(key: String): OptionT[F, Team] = OptionT(for {
    fbk <- Team.Dao.findByKey(key).option.transact(xa)
    fba <- Team.Dao.findByAlias(key).option.transact(xa)
  } yield {
    fbk.orElse(fba)
  })

  def findSeason(date: LocalDateTime): OptionT[F, Season] = OptionT(for {
    s <- Season.Dao.findByDate(date).option.transact(xa)
  } yield {
    s
  })

}
