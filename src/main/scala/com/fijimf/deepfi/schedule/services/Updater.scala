package com.fijimf.deepfi.schedule.services

import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfi.schedule.model.{Game, ProposedGame, Team}
import doobie.util.transactor.Transactor
import doobie.implicits._

class Updater [F[_] : Sync](xa: Transactor[F]) {

  def upsertGame(pg:ProposedGame) ={
    for {
      ht<-loadTeam(pg.homeKey)
      at<-loadTeam(pg.awayKey)
    } yield {
      for {
        h<-ht
        a<-at
      } yield {
        Game.Dao.findByDateTeams(pg.date, h.id, a.id).option.transact(xa)
      }
    }
  }

  def loadTeam(key:String):F[Option[Team]]={
    for{
      fbk <- Team.Dao.findByKey(key).option.transact(xa)
      fba <- Team.Dao.findByAlias(key).option.transact(xa)
    } yield {
      fbk.orElse(fba)
    }
  }

}
