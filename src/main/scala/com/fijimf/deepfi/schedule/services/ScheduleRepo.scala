package com.fijimf.deepfi.schedule.services

import cats.effect.Sync
import com.fijimf.deepfi.schedule.model._
import doobie.implicits._
import doobie.util.transactor.Transactor

class ScheduleRepo[F[_] : Sync](xa: Transactor[F]) extends AliasRepo[F] {

  override def insertAlias(a: Alias): F[Alias] = {
    import Alias.Dao._
    insert(a)
      .withUniqueGeneratedKeys[Alias](cols: _*)
      .transact(xa)
  }

  override def updateAlias(a: Alias): F[Alias] = {
    import Alias.Dao._
    update(a)
      .withUniqueGeneratedKeys[Alias](cols: _*)
      .transact(xa)
  }

  override def deleteAlias(id: Long): F[Int] = {
    Alias.Dao.delete(id).run.transact(xa)
  }

  override def listAliases(): F[List[Alias]] = Alias.Dao.list().to[List].transact(xa)

  override def findAlias(id: Long): F[Option[Alias]] = Alias.Dao.find(id).option.transact(xa)

  def insertConference(c: Conference): F[Conference] = {
    import Conference.Dao._
    insert(c)
      .withUniqueGeneratedKeys[Conference](cols: _*)
      .transact(xa)
  }

  def updateConference(c: Conference): F[Conference] = {
    import Conference.Dao._
    update(c)
      .withUniqueGeneratedKeys[Conference](cols: _*)
      .transact(xa)
  }

  def deleteConference(id: Long): F[Int] = {
    Conference.Dao.delete(id).run.transact(xa)
  }

  def listConferences(): F[List[Conference]] = Conference.Dao.list().to[List].transact(xa)

  def findConference(id: Long): F[Option[Conference]] = Conference.Dao.find(id).option.transact(xa)

  def insertConferenceMapping(cm: ConferenceMapping): F[ConferenceMapping] = {
    import ConferenceMapping.Dao._
    insert(cm)
      .withUniqueGeneratedKeys[ConferenceMapping](cols: _*)
      .transact(xa)
  }

  def updateConferenceMapping(cm: ConferenceMapping): F[ConferenceMapping] = {
    import ConferenceMapping.Dao._
    update(cm)
      .withUniqueGeneratedKeys[ConferenceMapping](cols: _*)
      .transact(xa)
  }

  def deleteConferenceMapping(id: Long): F[Int] = {
    ConferenceMapping.Dao.delete(id).run.transact(xa)
  }

  def listConferenceMappings(): F[List[ConferenceMapping]] = ConferenceMapping.Dao.list().to[List].transact(xa)

  def findConferenceMapping(id: Long): F[Option[ConferenceMapping]] = ConferenceMapping.Dao.find(id).option.transact(xa)

  def insertGame(g: Game): F[Game] = {
    import Game.Dao._
    insert(g)
      .withUniqueGeneratedKeys[Game](cols: _*)
      .transact(xa)
  }

  def updateGame(g: Game): F[Game] = {
    import Game.Dao._
    update(g)
      .withUniqueGeneratedKeys[Game](cols: _*)
      .transact(xa)
  }

  def deleteGame(id: Long): F[Int] = {
    Game.Dao.delete(id).run.transact(xa)
  }

  def listGame(): F[List[Game]] = Game.Dao.list().to[List].transact(xa)

  def findGame(id: Long): F[Option[Game]] = Game.Dao.find(id).option.transact(xa)

  def insertResult(r: Result): F[Result] = {
    import Result.Dao._
    insert(r)
      .withUniqueGeneratedKeys[Result](cols: _*)
      .transact(xa)
  }

  def updateResult(r: Result): F[Result] = {
    import Result.Dao._
    update(r)
      .withUniqueGeneratedKeys[Result](cols: _*)
      .transact(xa)
  }

  def deleteResult(id: Long): F[Int] = {
    Result.Dao.delete(id).run.transact(xa)
  }

  def listResult(): F[List[Result]] = Result.Dao.list().to[List].transact(xa)

  def findResult(id: Long): F[Option[Result]] = Result.Dao.find(id).option.transact(xa)

  def insertSeason(s: Season): F[Season] = {
    import Season.Dao._
    insert(s)
      .withUniqueGeneratedKeys[Season](cols: _*)
      .transact(xa)
  }

  def updateSeason(s: Season): F[Season] = {
    import Season.Dao._
    update(s)
      .withUniqueGeneratedKeys[Season](cols: _*)
      .transact(xa)
  }

  def deleteSeason(id: Long): F[Int] = {
    Season.Dao.delete(id).run.transact(xa)
  }

  def listSeason(): F[List[Season]] = Season.Dao.list().to[List].transact(xa)

  def findSeason(id: Long): F[Option[Season]] = Season.Dao.find(id).option.transact(xa)

  def insertTeam(t: Team): F[Team] = {
    import Team.Dao._
    insert(t)
      .withUniqueGeneratedKeys[Team](cols: _*)
      .transact(xa)
  }

  def updateTeam(t: Team): F[Team] = {
    import Team.Dao._
    update(t)
      .withUniqueGeneratedKeys[Team](cols: _*)
      .transact(xa)
  }

  def deleteTeam(id: Long): F[Int] = {
    Team.Dao.delete(id).run.transact(xa)
  }

  def listTeam(): F[List[Team]] = Team.Dao.list().to[List].transact(xa)

  def findTeam(id: Long): F[Option[Team]] = Team.Dao.find(id).option.transact(xa)


}
