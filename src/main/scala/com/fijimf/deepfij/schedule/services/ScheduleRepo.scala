package com.fijimf.deepfij.schedule.services

import cats.MonadError
import cats.effect.Sync
import com.fijimf.deepfij.schedule.model._
import doobie.implicits._
import doobie.util.transactor.Transactor

class ScheduleRepo[F[_] : Sync](xa: Transactor[F]) extends AliasRepo[F] with ConferenceRepo[F] with ConferenceMappingRepo[F] with GameRepo[F] with TeamRepo[F] with SeasonRepo[F] with ResultRepo[F] {

  override def insertAlias(a: Alias)(implicit me: MonadError[F, Throwable]): F[Alias] = {
    import Alias.Dao._
    insert(a)
      .withUniqueGeneratedKeys[Alias](cols: _*)
      .transact(xa).exceptSql(ex=>me.raiseError[Alias](ex))
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

  override def insertConference(c: Conference): F[Conference] = {
    import Conference.Dao._
    insert(c)
      .withUniqueGeneratedKeys[Conference](cols: _*)
      .transact(xa)
  }

  override def updateConference(c: Conference): F[Conference] = {
    import Conference.Dao._
    update(c)
      .withUniqueGeneratedKeys[Conference](cols: _*)
      .transact(xa)
  }

  override def deleteConference(id: Long): F[Int] = {
    Conference.Dao.delete(id).run.transact(xa)
  }

  override def listConferences(): F[List[Conference]] = Conference.Dao.list().to[List].transact(xa)

  override def findConference(id: Long): F[Option[Conference]] = Conference.Dao.find(id).option.transact(xa)

  override def insertConferenceMapping(cm: ConferenceMapping): F[ConferenceMapping] = {
    import ConferenceMapping.Dao._
    insert(cm)
      .withUniqueGeneratedKeys[ConferenceMapping](cols: _*)
      .transact(xa)
  }

  override def updateConferenceMapping(cm: ConferenceMapping): F[ConferenceMapping] = {
    import ConferenceMapping.Dao._
    update(cm)
      .withUniqueGeneratedKeys[ConferenceMapping](cols: _*)
      .transact(xa)
  }

  override def deleteConferenceMapping(id: Long): F[Int] = {
    ConferenceMapping.Dao.delete(id).run.transact(xa)
  }

  override def listConferenceMappings(): F[List[ConferenceMapping]] = ConferenceMapping.Dao.list().to[List].transact(xa)

  override def findConferenceMapping(id: Long): F[Option[ConferenceMapping]] = ConferenceMapping.Dao.find(id).option.transact(xa)

  override def insertGame(g: Game): F[Game] = {
    import Game.Dao._
    insert(g)
      .withUniqueGeneratedKeys[Game](cols: _*)
      .transact(xa)
  }

  override def updateGame(g: Game): F[Game] = {
    import Game.Dao._
    update(g)
      .withUniqueGeneratedKeys[Game](cols: _*)
      .transact(xa)
  }

  override def deleteGame(id: Long): F[Int] = {
    Game.Dao.delete(id).run.transact(xa)
  }

  override def listGame(): F[List[Game]] = Game.Dao.list().to[List].transact(xa)

  override def findGame(id: Long): F[Option[Game]] = Game.Dao.find(id).option.transact(xa)

  override def insertResult(r: Result): F[Result] = {
    import Result.Dao._
    insert(r)
      .withUniqueGeneratedKeys[Result](cols: _*)
      .transact(xa)
  }

  override def updateResult(r: Result): F[Result] = {
    import Result.Dao._
    update(r)
      .withUniqueGeneratedKeys[Result](cols: _*)
      .transact(xa)
  }

  override def deleteResult(id: Long): F[Int] = {
    Result.Dao.delete(id).run.transact(xa)
  }

  override def listResult(): F[List[Result]] = Result.Dao.list().to[List].transact(xa)

  override def findResult(id: Long): F[Option[Result]] = Result.Dao.find(id).option.transact(xa)

  override def insertSeason(s: Season): F[Season] = {
    import Season.Dao._
    insert(s)
      .withUniqueGeneratedKeys[Season](cols: _*)
      .transact(xa)
  }

  override def updateSeason(s: Season): F[Season] = {
    import Season.Dao._
    update(s)
      .withUniqueGeneratedKeys[Season](cols: _*)
      .transact(xa)
  }

  override def deleteSeason(id: Long): F[Int] = {
    Season.Dao.delete(id).run.transact(xa)
  }

  override def listSeason(): F[List[Season]] = Season.Dao.list().to[List].transact(xa)

  override def findSeason(id: Long): F[Option[Season]] = Season.Dao.find(id).option.transact(xa)

  override def insertTeam(t: Team): F[Team] = {
    import Team.Dao._
    insert(t)
      .withUniqueGeneratedKeys[Team](cols: _*)
      .transact(xa)
  }

  override def updateTeam(t: Team): F[Team] = {
    import Team.Dao._
    update(t)
      .withUniqueGeneratedKeys[Team](cols: _*)
      .transact(xa)
  }

  override def deleteTeam(id: Long): F[Int] = {
    Team.Dao.delete(id).run.transact(xa)
  }

  override def listTeam(): F[List[Team]] = Team.Dao.list().to[List].transact(xa)

  override def findTeam(id: Long): F[Option[Team]] = Team.Dao.find(id).option.transact(xa)


}
