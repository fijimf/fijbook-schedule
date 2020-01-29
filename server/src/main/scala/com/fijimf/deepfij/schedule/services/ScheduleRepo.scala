package com.fijimf.deepfij.schedule.services

import cats.MonadError
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.model._
import doobie.implicits._
import doobie.util.transactor.Transactor
import com.fijimf.deepfij.schedule.model.Dao._

class ScheduleRepo[F[_] : Sync](xa: Transactor[F]) extends AliasRepo[F] with ConferenceRepo[F] with ConferenceMappingRepo[F] with GameRepo[F] with TeamRepo[F] with SeasonRepo[F] with ResultRepo[F] {

  val me: MonadError[F, Throwable] = implicitly[MonadError[F, Throwable]]

  def healthcheck:F[Boolean] = {
    doobie.FC.isValid(2 /*timeout in seconds*/).transact(xa)
  }

  override def insertAlias(a: Alias): F[Alias] = {
    import AliasDao._
    insert(a)
      .withUniqueGeneratedKeys[Alias](cols: _*)
      .transact(xa).exceptSql(ex=>me.raiseError[Alias](ex))
  }

  override def updateAlias(a: Alias): F[Alias] = {
    import AliasDao._
    update(a)
      .withUniqueGeneratedKeys[Alias](cols: _*)
      .transact(xa)
  }

  override def deleteAlias(id: Long): F[Int] = {
    AliasDao.delete(id).run.transact(xa)
  }

  override def listAliases(): F[List[Alias]] = AliasDao.list().to[List].transact(xa)

  override def findAlias(id: Long): F[Option[Alias]] = AliasDao.find(id).option.transact(xa)

  override def insertConference(c: Conference): F[Conference] = {
    import ConferenceDao._
    insert(c)
      .withUniqueGeneratedKeys[Conference](cols: _*)
      .transact(xa)
  }

  override def updateConference(c: Conference): F[Conference] = {
    import ConferenceDao._
    update(c)
      .withUniqueGeneratedKeys[Conference](cols: _*)
      .transact(xa)
  }

  override def deleteConference(id: Long): F[Int] = {
    ConferenceDao.delete(id).run.transact(xa)
  }

  override def listConferences(): F[List[Conference]] = ConferenceDao.list().to[List].transact(xa)

  override def findConference(id: Long): F[Option[Conference]] = ConferenceDao.find(id).option.transact(xa)

  override def insertConferenceMapping(cm: ConferenceMapping): F[ConferenceMapping] = {
    import ConferenceMappingDao._
    insert(cm)
      .withUniqueGeneratedKeys[ConferenceMapping](cols: _*)
      .transact(xa)
  }

  override def updateConferenceMapping(cm: ConferenceMapping): F[ConferenceMapping] = {
    import ConferenceMappingDao._
    update(cm)
      .withUniqueGeneratedKeys[ConferenceMapping](cols: _*)
      .transact(xa)
  }

  override def deleteConferenceMapping(id: Long): F[Int] = {
    ConferenceMappingDao.delete(id).run.transact(xa)
  }

  override def listConferenceMappings(): F[List[ConferenceMapping]] = ConferenceMappingDao.list().to[List].transact(xa)

  override def findConferenceMapping(id: Long): F[Option[ConferenceMapping]] = ConferenceMappingDao.find(id).option.transact(xa)

  override def findConferenceMappingBySeason(id: Long) :F[List[ConferenceMapping]] = ConferenceMappingDao.findBySeason(id).to[List].transact(xa)

  override def insertGame(g: Game): F[Game] = {
    import GameDao._
    insert(g)
      .withUniqueGeneratedKeys[Game](cols: _*)
      .transact(xa)
  }

  override def updateGame(g: Game): F[Game] = {
    import GameDao._
    update(g)
      .withUniqueGeneratedKeys[Game](cols: _*)
      .transact(xa)
  }

  override def deleteGame(id: Long): F[Int] = {
    GameDao.delete(id).run.transact(xa)
  }

  override def listGame(): F[List[Game]] = GameDao.list().to[List].transact(xa)

  override def findGame(id: Long): F[Option[Game]] = GameDao.find(id).option.transact(xa)

  override def findGamesBySeason(id: Long):F[List[Game]] = GameDao.findBySeason(id).to[List].transact(xa)

  override def insertResult(r: Result): F[Result] = {
    import ResultDao._
    insert(r)
      .withUniqueGeneratedKeys[Result](cols: _*)
      .transact(xa)
  }

  override def updateResult(r: Result): F[Result] = {
    import ResultDao._
    update(r)
      .withUniqueGeneratedKeys[Result](cols: _*)
      .transact(xa)
  }

  override def deleteResult(id: Long): F[Int] = {
    ResultDao.delete(id).run.transact(xa)
  }

  override def listResult(): F[List[Result]] = ResultDao.list().to[List].transact(xa)

  override def findResult(id: Long): F[Option[Result]] = ResultDao.find(id).option.transact(xa)

  override def findResultsBySeason(id: Long):F[List[Result]] = ResultDao.findBySeason(id).to[List].transact(xa)

  override def insertSeason(s: Season): F[Season] = {
    import SeasonDao._
    insert(s)
      .withUniqueGeneratedKeys[Season](cols: _*)
      .transact(xa)
  }

  override def updateSeason(s: Season): F[Season] = {
    import SeasonDao._
    update(s)
      .withUniqueGeneratedKeys[Season](cols: _*)
      .transact(xa)
  }

  override def deleteSeason(id: Long): F[Int] = {
    SeasonDao.delete(id).run.transact(xa)
  }

  override def listSeason(): F[List[Season]] = SeasonDao.list().to[List].transact(xa)

  override def findSeason(id: Long): F[Option[Season]] = SeasonDao.find(id).option.transact(xa)

  override def findLatestSeason(): F[Option[Season]] = SeasonDao.findLatest().option.transact(xa)

  override def findSeasonByYear(y:Int): F[Option[Season]] = SeasonDao.findByYear(y).option.transact(xa)


  override def insertTeam(t: Team): F[Team] = {
    import TeamDao._
    insert(t)
      .withUniqueGeneratedKeys[Team](cols: _*)
      .transact(xa)
  }

  override def updateTeam(t: Team): F[Team] = {
    import TeamDao._
    update(t)
      .withUniqueGeneratedKeys[Team](cols: _*)
      .transact(xa)
  }

  override def deleteTeam(id: Long): F[Int] = {
    TeamDao.delete(id).run.transact(xa)
  }

  override def listTeam(): F[List[Team]] = TeamDao.list().to[List].transact(xa)

  override def findTeam(id: Long): F[Option[Team]] = TeamDao.find(id).option.transact(xa)


  def loadScheduleRoot(): F[ScheduleRoot] = {
    for {
      teams <- listTeam()
      conferences <- listConferences()
      seasons <- listSeason()
      conferenceMappings <- listConferenceMappings()
      games <- listGame()
      results <- listResult()
    } yield {
      ScheduleRoot(teams, conferences, seasons, conferenceMappings, games, results)
    }
  }

}
