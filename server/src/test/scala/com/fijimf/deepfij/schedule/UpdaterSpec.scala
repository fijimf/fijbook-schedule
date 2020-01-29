package com.fijimf.deepfij.schedule

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import cats.effect.IO
import com.fijimf.deepfij.schedule.model.Dao.{AliasDao, GameDao, ResultDao, SeasonDao, TeamDao}
import com.fijimf.deepfij.schedule.model.{Alias, Game, Result, Season, Team, UpdateCandidate}
import com.fijimf.deepfij.schedule.services.{ScheduleRepo, Updater}
import doobie.implicits._

class UpdaterSpec extends DbIntegrationSpec {
  val containerName = "updater-spec"
  val port="7377"

  describe("Updater"){
    val repo: ScheduleRepo[IO] = new ScheduleRepo[IO](transactor)
    val updater: Updater[IO] = Updater(transactor)

    def updaterSetup(): Unit = {

      (for {
        _ <- AliasDao.truncate().run.transact(transactor)
        _ <- TeamDao.truncate().run.transact(transactor)
        _ <- GameDao.truncate().run.transact(transactor)
        _ <- ResultDao.truncate().run.transact(transactor)
        _ <- SeasonDao.truncate().run.transact(transactor)
        s <- repo.insertSeason(Season(0L, 2020))
        georgetown <- repo.insertTeam(Team(0L, "georgetown", "Georgetown", "Hoyas", "", "", ""))
        harvard <- repo.insertTeam(Team(0L, "harvard", "Harvard", "Crimson", "", "", ""))
        duke <- repo.insertTeam(Team(0L, "duke", "Duke", "Blue Devils", "", "", ""))
        carolina <- repo.insertTeam(Team(0L, "north-carolina", "North Carolina", "Tar Heels", "", "", ""))
        villanova <- repo.insertTeam(Team(0L, "villanova", "Villanove", "Wildcats", "", "", ""))
        _ <- repo.insertAlias(Alias(0L, carolina.id, "unc"))
      } yield {

      }).unsafeRunSync
    }


    it("should initially have no games") {
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
      }).unsafeRunSync()
    }

    it("should lookup teams by key") {
      updaterSetup()
      import cats.implicits._
      (for {
        teams <- repo.listTeam()
        list <- teams.map(t => updater.loadTeam(t.key).value.map(_.toList)).sequence
      } yield {
        assert(teams === list.flatten)
      }).unsafeRunSync()
    }

    it("should lookup teams by alias") {
      updaterSetup()
      import cats.implicits._
      (for {
        aliases <- repo.listAliases()
        list <- aliases.map(a => updater.loadTeam(a.alias).value.map(_.toList)).sequence
      } yield {
        val aliasTeamIds: List[Long] = aliases.map(_.teamId)
        val teamIds: List[Long] = list.flatten.map(_.id)
        assert(!(aliasTeamIds =!= teamIds))
      }).unsafeRunSync()
    }

    it("should not find bogus teams") {
      updaterSetup()
      import cats.implicits._
      val strings = List("junk", "don't find me")
      (for {
        list <- strings.map(s => updater.loadTeam(s).value.map(_.toList)).sequence
      } yield {
        assert(list.flatten.isEmpty)
      }).unsafeRunSync()
    }

    it("should identify correct gamekeys") {

      val time: LocalDateTime = LocalDateTime.of(2019, 11, 15, 19, 30)
      val loadKey: String = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val updates = List(
        UpdateCandidate(time, "georgetown", "duke", Some("Verizon Center"), Some(false), None, None, None)
      )
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
        aliases <- repo.listAliases()
        keyMap <- updater.findKeys(updates, loadKey)
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
        assert(aliases.size === 1)
        assert(keyMap.size ===1 )
        keyMap.headOption.foreach{case (k,(g,or))=>
          assert(teams.find(_.id===k.homeTeamId).map(_.nickname)===Some("Hoyas"))
          assert(teams.find(_.id===k.awayTeamId).map(_.nickname)===Some("Blue Devils"))
          assert(k.seasonId>0L)
        }
      }).unsafeRunSync()
    }

    it("insert 1 new game") {

      val time: LocalDateTime = LocalDateTime.of(2019, 11, 15, 19, 30)
      val loadKey: String = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val updates = List(
        UpdateCandidate(time, "georgetown", "duke", Some("Verizon Center"), Some(false), None, None, None)
      )
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
        aliases <- repo.listAliases()
        changes <- updater.updateGamesAndResults(updates, loadKey)
        gamesPost <- repo.listGame()
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
        assert(aliases.size === 1)
        assert(changes.size ===1 )
        assert(gamesPost.size ===1 )
      }).unsafeRunSync()
    }

    it("insert 1 new game then delete it") {

      val time: LocalDateTime = LocalDateTime.of(2019, 11, 15, 19, 30)
      val loadKey: String = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val updates = List(
        UpdateCandidate(time, "georgetown", "duke", Some("Verizon Center"), Some(false), None, None, None)
      )
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
        aliases <- repo.listAliases()
        changes <- updater.updateGamesAndResults(updates, loadKey)
        gamesPostAdd <- repo.listGame()
        changes2 <- updater.updateGamesAndResults(List.empty[UpdateCandidate], loadKey)
        gamesPostDelete <- repo.listGame()
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
        assert(aliases.size === 1)
        assert(changes.size ===1 )
        assert(gamesPostAdd.size ===1 )
        assert(gamesPostDelete.isEmpty )
      }).unsafeRunSync()
    }

    it("insert 1 new game then update the game") {

      val time: LocalDateTime = LocalDateTime.of(2019, 11, 15, 19, 30)
      val loadKey: String = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val updates = List(
        UpdateCandidate(time, "georgetown", "duke", Some("MCI Center"), Some(false), None, None, None)
      )
      val updates1 = List(
        UpdateCandidate(time, "georgetown", "duke", Some("Verizon Center"), Some(false), None, None, None)
      )
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
        aliases <- repo.listAliases()
        changes <- updater.updateGamesAndResults(updates, loadKey)
        gamesPostAdd <- repo.listGame()
        changes2 <- updater.updateGamesAndResults(updates1, loadKey)
        gamesPostUpdate <- repo.listGame()
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
        assert(aliases.size === 1)
        assert(changes.size ===1 )
        assert(gamesPostAdd.size ===1 )
        assert(gamesPostUpdate.size ===1  )
        assert(gamesPostAdd.headOption.map(_.id)===gamesPostUpdate.headOption.map(_.id))
        assert(!(gamesPostAdd.headOption.map(_.location)===gamesPostUpdate.headOption.map(_.location)))
      }).unsafeRunSync()
    }


    it("insert 1 new game, then add a result") {

      val time: LocalDateTime = LocalDateTime.of(2019, 11, 15, 19, 30)
      val loadKey: String = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val updates: List[UpdateCandidate] = List(
        UpdateCandidate(time, "georgetown", "duke", Some("Verizon Center"), Some(false), None, None, None)
      )
      val updates2: List[UpdateCandidate] = List(
        UpdateCandidate(time, "georgetown", "duke", Some("Verizon Center"), Some(false), Some(100), Some(32), Some(2))
      )
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
        aliases <- repo.listAliases()
        changes <- updater.updateGamesAndResults(updates, loadKey)
        gamesPostAdd <- repo.listGame()
        resultsPostAdd <- repo.listResult()
        changes <- updater.updateGamesAndResults(updates2, loadKey)
        gamesPostResult <- repo.listGame()
        resultsPostResult <- repo.listResult()
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
        assert(aliases.size === 1)
        assert(changes.size === 1)
        assert(gamesPostAdd === gamesPostResult)
        assert(resultsPostAdd.size === 0)
        assert(resultsPostResult.size === 1)
      }).unsafeRunSync()
    }

    it("insert 1 new game, then update the game and add a result") {

      val time: LocalDateTime = LocalDateTime.of(2019, 11, 15, 19, 30)
      val loadKey: String = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val updates: List[UpdateCandidate] = List(
        UpdateCandidate(time, "georgetown", "duke", Some("MCI Center"), Some(false), None, None, None)
      )
      val updates2: List[UpdateCandidate] = List(
        UpdateCandidate(time, "georgetown", "duke", Some("Verizon Center"), Some(false), Some(100), Some(32), Some(2))
      )
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
        aliases <- repo.listAliases()
        changes <- updater.updateGamesAndResults(updates, loadKey)
        gamesPostAdd <- repo.listGame()
        resultsPostAdd <- repo.listResult()
        changes <- updater.updateGamesAndResults(updates2, loadKey)
        gamesPostResult <- repo.listGame()
        resultsPostResult <- repo.listResult()
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
        assert(aliases.size === 1)
        assert(changes.size === 1)
        assert(resultsPostAdd.size === 0)
        assert(resultsPostResult.size === 1)
        assert(gamesPostAdd.headOption.map(_.id)===gamesPostResult.headOption.map(_.id))
        assert(!(gamesPostAdd.headOption.map(_.location)===gamesPostResult.headOption.map(_.location)))
      }).unsafeRunSync()
    }

  }


}
