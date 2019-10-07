package com.fijimf.deepfi.schedule.services

import java.time.{LocalDate, LocalDateTime}

import cats.data.OptionT
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfi.schedule.model.{Game, ProposedGame, Season, Team}
import doobie.implicits._
import doobie.util.transactor.Transactor

class Updater [F[_] : Sync](xa: Transactor[F]) {

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
    for {
      og <- findMatch(pg.date, s, ht, at)
      fg <- og match {
        case Some(g) =>
          val g1: Game = g.copy(time = pg.dateTime, location = pg.location, isNeutral = pg.isNeutral, loadKey = loadKey)
          update(g1).withUniqueGeneratedKeys[Game](cols: _*).transact(xa)
        case None =>
          val g0: Game = Game(0L, s.id, pg.date, pg.dateTime, ht.id, at.id, pg.location, pg.isNeutral, loadKey)
          insert(g0).withUniqueGeneratedKeys[Game](cols: _*).transact(xa)
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
