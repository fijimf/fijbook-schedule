package com.fijimf.deepfi.schedule.services

import java.time.{LocalDate, LocalDateTime}

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import cats.kernel.Eq
import com.fijimf.deepfi.schedule.model._
import doobie.implicits._
import doobie.util.transactor.Transactor

class Updater[F[_]](xa: Transactor[F])(implicit F: Sync[F]) {

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
  def updateGamesAndResults(pgs: List[ProposedGame], pgrs: List[ProposedGameResult], loadKey: String): Unit = {



  }


  def upsertGame(pg: ProposedGame, loadKey: String): F[Option[Game]] = {
    (for {
      ht <- loadTeam(pg.homeKey)
      at <- loadTeam(pg.awayKey)
      s <- findSeason(pg.dateTime)
      g <- OptionT.liftF(insertOrUpdate(pg, s, ht, at, loadKey))
    } yield {
      g
    }).value
  }

  private def findMatch(d: LocalDate, s: Season, ht: Team, at: Team): F[Option[Game]] = {
    Game.Dao.findByDateTeams(d, ht.id, at.id).option.transact(xa)
  }

  private def insertOrUpdate(pg: ProposedGame, s: Season, ht: Team, at: Team, loadKey: String): F[Game] = {
    import Game.Dao._
    val game: Game = pg.toGame(0L, s.id, ht.id, at.id, loadKey)
    for {
      og <- findMatch(pg.date, s, ht, at)
      fg <- og match {
        case Some(g) =>
          val g1: Game = game.copy(id = g.id)
          if (g =!= g1) {
            update(g1).withUniqueGeneratedKeys[Game](cols: _*).transact(xa)
          } else {
            F.pure(g1)
          }
        case None =>
          insert(game).withUniqueGeneratedKeys[Game](cols: _*).transact(xa)
      }
    } yield {
      fg
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
