package com.fijimf.deepfij.schedule

import java.sql.SQLException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import cats.effect.IO
import com.fijimf.deepfij.schedule.model._
import com.fijimf.deepfij.schedule.services.ScheduleRepo
import doobie.implicits._

class ScheduleRepoSpec extends DbIntegrationSpec {
  val containerName = "schedule-repo-spec"
  val port="7375"

  describe("Schedule repo ops") {
    val repo = new ScheduleRepo[IO](transactor)
    val georgetown = Team(0L, "georgetown", "Georgetown", "Hoyas", "", "blue", "gray")
    val villanova = Team(0L, "villanova", "Villanova", "Wildcats", "", "blue", "")
    val season = Season(0L, 2014)
    val bigEast = Conference(0L, "big-east", "Big East", "The Big East Conference", Some("http://Blah.blah.com/logo.png"))
    val bigTen = Conference(0L, "big-10", "Big Ten", "The Big Ten Conference", Some("http://Blah.blah.com/logo.png"))

    describe("Alias ops") {
      it("should list all aliases") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          aliasList <- repo.listAliases()
        } yield {
          assert(aliasList.size === 0)
        }).unsafeRunSync()
      }

      it("should insert an alias") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          t<-repo.insertTeam(georgetown)
          aliasList0 <- repo.listAliases()
          a <- repo.insertAlias(Alias(0L, t.id, "gtown"))
          aliasList1 <- repo.listAliases()
        } yield {
          assert(a.id > 0L)
          assert(aliasList1.size >= aliasList0.size)
          assert(aliasList1.contains(a))
        }).unsafeRunSync()
      }

      it("should find aliases") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          t<-repo.insertTeam(georgetown)
          a <- repo.insertAlias(Alias(0L, t.id, "gtown"))
          a1 <- repo.findAlias(a.id)
          ax <- repo.findAlias(-999L)
        } yield {
          assert(a1 === Some(a))
          assert(ax.isEmpty)
        }).unsafeRunSync()
      }

      it("should update an alias") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          t<-repo.insertTeam(georgetown)
          a <- repo.insertAlias(Alias(0L, t.id, "gtown"))
          a1 <- repo.findAlias(a.id)
          a2 <- repo.updateAlias(a.copy(alias = "GU"))
          a3 <- repo.findAlias(a.id)
        } yield {
          assert(a1 === Some(a))
          assert(!(a === a2))
          assert(a3 === Some(a2))
        }).unsafeRunSync()
      }

      it("should delete an alias") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          t<-repo.insertTeam(georgetown)
          a <- repo.insertAlias(Alias(0L, t.id, "gtown"))
          list1 <- repo.listAliases()
          n <- repo.deleteAlias(a.id)
          list2 <- repo.listAliases()
          m <- repo.deleteAlias(-999L)
        } yield {
          assert(list1.contains(a))
          assert(n === 1)
          assert(!list2.contains(a))
          assert(m === 0)
        }).unsafeRunSync()
      }
    }
    describe("Conference ops"){
      it("should list conferences"){
        (for {
          _ <- Conference.Dao.truncate().run.transact(transactor)
          conferenceList <- repo.listConferences()
        } yield {
          assert(conferenceList.size === 0)
        }).unsafeRunSync()
      }

      it ("should insert a conference"){
        (for {
          _ <- Conference.Dao.truncate().run.transact(transactor)
          conferenceList <- repo.listConferences()
          c<- repo.insertConference(bigEast)
          conferenceList1<-repo.listConferences()
        } yield {
          assert(c.id >0L)
          assert(conferenceList.size+1===conferenceList1.size)
          assert(conferenceList1.contains(c))
        }).unsafeRunSync()
      }

      it ("should find a conferences"){
        (for {
          _ <- Conference.Dao.truncate().run.transact(transactor)
          c<- repo.insertConference(Conference(0L, "pac12","Pac-12", "The Pac-12 Conference", Some("http://Blah.blah.com/logo.png")))
          c1<-repo.findConference(c.id)
          cx<-repo.findConference(-999L)
        } yield {
          assert(c1===Some(c))
          assert(cx.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a conferences"){
        (for {
          _ <- Conference.Dao.truncate().run.transact(transactor)
          c<- repo.insertConference(Conference(0L, "big12","Big-12", "The Big-12 Conference", Some("http://Blah.blah.com/logo.png")))
          c1<-repo.findConference(c.id)
          c2<-repo.updateConference(c.copy(name="Big Twelve"))
          c3<-repo.findConference(c.id)
        } yield {
          assert(c1===Some(c))
          assert(c3===Some(c2))
          assert(!(c===c2))
        }).unsafeRunSync()

      }
      it("should delete a conferences"){
        (for {
          _ <- Conference.Dao.truncate().run.transact(transactor)
          c<- repo.insertConference(Conference(0L, "big-west","Big West", "The Big West Conference", Some("http://Blah.blah.com/logo.png")))
          c1<-repo.findConference(c.id)
          n<-repo.deleteConference(c.id)
          c3<-repo.findConference(c.id)
        } yield {
          assert(c1===Some(c))
          assert(n===1)
          assert(c3===None)
        }).unsafeRunSync()

      }

    }
    describe("ConferenceMapping ops"){
      it("should list conferenceMappings"){
        (for {
          _ <- Conference.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Team.Dao.truncate().run.transact(transactor)
          t <-repo.insertTeam(georgetown)
          s<-repo.insertSeason(season)
          c<-repo.insertConference(bigEast)
          cmList <- repo.listConferenceMappings()
        } yield {
          assert(cmList.size === 0)
        }).unsafeRunSync()
      }

      it ("should insert a conferenceMapping"){
        (for {
          _ <- Conference.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Team.Dao.truncate().run.transact(transactor)
          t <-repo.insertTeam(georgetown)
          s<-repo.insertSeason(season)
          c<-repo.insertConference(bigEast)

          cmList <- repo.listConferenceMappings()
          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, s.id,t.id,c.id))
          cmList1<-repo.listConferenceMappings()
        } yield {
          assert(cm.id >0L)
          assert(cmList.size+1===cmList1.size)
          assert(cmList1.contains(cm))
        }).unsafeRunSync()
      }

      it ("should find a conferenceMapping"){
        (for {
          _ <- Conference.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Team.Dao.truncate().run.transact(transactor)
          t <-repo.insertTeam(georgetown)
          s<-repo.insertSeason(season)
          c<-repo.insertConference(bigEast)

          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, s.id,t.id,c.id))
          cm1<-repo.findConferenceMapping(cm.id)
          cmx<-repo.findConferenceMapping(-999L)
        } yield {
          assert(cm1===Some(cm))
          assert(cmx.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a conferenceMapping"){
        (for {
          _ <- Conference.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Team.Dao.truncate().run.transact(transactor)
          t <-repo.insertTeam(georgetown)
          s<-repo.insertSeason(season)
          c<-repo.insertConference(bigEast)
          c2<-repo.insertConference(bigTen)

          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, s.id,t.id,c.id))
          cm1<-repo.findConferenceMapping(cm.id)
          cm2<-repo.updateConferenceMapping(cm.copy(conferenceId=c2.id))
          cm3<-repo.findConferenceMapping(cm.id)
        } yield {
          assert(cm1===Some(cm))
          assert(cm3===Some(cm2))
          assert(!(cm===cm2))
        }).unsafeRunSync()
      }

      it("should delete a conferenceMapping"){
        (for {
          _ <- Conference.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Team.Dao.truncate().run.transact(transactor)
          t <-repo.insertTeam(georgetown)
          s<-repo.insertSeason(season)
          c<-repo.insertConference(bigEast)

          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, s.id,t.id,c.id))
          cm1<-repo.findConferenceMapping(cm.id)
          n<-repo.deleteConferenceMapping(cm.id)
          cm3<-repo.findConferenceMapping(cm.id)
        } yield {
          assert(cm1===Some(cm))
          assert(n===1)
          assert(cm3===None)
        }).unsafeRunSync()
      }

    }
    describe("Game ops"){
      val time: LocalDateTime = LocalDateTime.now()
      val g = Game(0L, 1L, time.toLocalDate, time, 2L, 3L, None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
      it("should list games") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          gameList <- repo.listGame()
        } yield {
          assert(gameList.size >= 0)
        }).unsafeRunSync()
      }

      it("should insert a game") {

        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          gameList <- repo.listGame()
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          game <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          gameList1 <- repo.listGame()
        } yield {
          assert(game.id > 0L)
          assert(gameList.size + 1 === gameList1.size)
          assert(gameList1.contains(game))
        }).unsafeRunSync()
      }

      it("should find a game") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          game <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          game1 <- repo.findGame(game.id)
          gamex <- repo.findGame(-999L)
        } yield {
          assert(game1 === Some(game))
          assert(gamex.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a game") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          game <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          game1 <- repo.findGame(game.id)
          game2 <- repo.updateGame(game.copy(isNeutral = Some(true)))
          game3 <- repo.findGame(game.id)
        } yield {
          assert(game1 === Some(game))
          assert(game3 === Some(game2))
          assert(!(game === game2))
        }).unsafeRunSync()
      }

      it("should delete a game") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          game <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          game1 <- repo.findGame(game.id)
          n <- repo.deleteGame(game.id)
          game3 <- repo.findGame(game.id)
        } yield {
          assert(game1 === Some(game))
          assert(n === 1)
          assert(game3 === None)
        }).unsafeRunSync()
      }

    }
    describe("Result ops"){
      val time: LocalDateTime = LocalDateTime.now()
      it("should list results") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Game.Dao.truncate().run.transact(transactor)
          resList <- repo.listResult()
        } yield {
          assert(resList.size === 0)
        }).unsafeRunSync()
      }

      it("should insert a result") {

        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Game.Dao.truncate().run.transact(transactor)
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          g <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          resList <- repo.listResult()
          res <- repo.insertResult(Result(0L, g.id, 67,89,2))
          resList1 <- repo.listResult()
        } yield {
          assert(res.id > 0L)
          assert(resList.size + 1 === resList1.size)
          assert(resList1.contains(res))
        }).unsafeRunSync()
      }

      it("should find a result") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Game.Dao.truncate().run.transact(transactor)
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          g <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          result <- repo.insertResult(Result(0L, g.id, 67,89,2))
          result1 <- repo.findResult(result.id)
          resultx <- repo.findResult(-999L)
        } yield {
          assert(result1 === Some(result))
          assert(resultx.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a result") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Game.Dao.truncate().run.transact(transactor)
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          g <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          result <- repo.insertResult(Result(0L, g.id, 67,89,2))
          result1 <- repo.findResult(result.id)
          result2 <- repo.updateResult(result.copy(numPeriods = 3))
          result3 <- repo.findResult(result.id)
        } yield {
          assert(result1 === Some(result))
          assert(result3 === Some(result2))
          assert(!(result === result2))
        }).unsafeRunSync()
      }

      it("should delete a result") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Game.Dao.truncate().run.transact(transactor)
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          g <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          result <- repo.insertResult(Result(0L, g.id, 67,89,2))
          result1 <- repo.findResult(result.id)
          n <- repo.deleteResult(result.id)
          result3 <- repo.findResult(result.id)
        } yield {
          assert(result1 === Some(result))
          assert(n === 1)
          assert(result3 === None)
        }).unsafeRunSync()
      }

      it("insert should fail on duplicate game_id") {
        val thr: SQLException = intercept[SQLException]((for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Game.Dao.truncate().run.transact(transactor)
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          g <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          _ <- repo.insertResult(Result(0L, g.id, 67,89,2))
          _ <- repo.insertResult(Result(0L, g.id, 44,93,2))
        } yield {
        }).unsafeRunSync())
        assert(thr.getMessage.contains("duplicate key value violates unique constraint"))
      }

      it("update should fail on duplicate game_id") {
        val thr: SQLException = intercept[SQLException]((for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Game.Dao.truncate().run.transact(transactor)
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          g <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          h <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t2.id,t1.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          r <- repo.insertResult(Result(0L, g.id, 67,89,2))
          s <- repo.insertResult(Result(0L, h.id, 44,93,2))
          _ <- repo.updateResult(s.copy(gameId = g.id))
        } yield {
        }).unsafeRunSync())
        assert(thr.getMessage.contains("duplicate key value violates unique constraint"))
      }

      it("insert should fail on missing game_id") {
        val thr: SQLException = intercept[SQLException]((for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Game.Dao.truncate().run.transact(transactor)
          _ <- repo.insertResult(Result(0L, 12L, 67,89,2))
        } yield {
        }).unsafeRunSync())
        assert(thr.getMessage.contains("violates foreign key constraint"))
      }

      it("update should fail on missing game_id") {
        val thr: SQLException = intercept[SQLException]((for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          _ <- Season.Dao.truncate().run.transact(transactor)
          _ <- Game.Dao.truncate().run.transact(transactor)
          t1<-repo.insertTeam(georgetown)
          t2<-repo.insertTeam(villanova)
          s<-repo.insertSeason(season)
          g <- repo.insertGame(Game(0L,s.id,time.toLocalDate, time,t1.id,t2.id,None, Some(false), time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
          r <- repo.insertResult(Result(0L, g.id, 67,89,2))
          _ <- repo.updateResult(r.copy(gameId = -r.gameId))
        } yield {
        }).unsafeRunSync())
        assert(thr.getMessage.contains("violates foreign key constraint"))
      }

    }
    describe("Season ops"){

      it("should list seasons"){
        (for {
          _ <- Season.Dao.truncate().run.transact(transactor)
          seasonList <- repo.listSeason()
        } yield {
          assert(seasonList.size === 0)
        }).unsafeRunSync()
      }

      it ("should insert a season"){
        (for {
          _ <- Season.Dao.truncate().run.transact(transactor)
          seasonList <- repo.listSeason()
          s <- repo.insertSeason(season)
          seasonList1<-repo.listSeason()
        } yield {
          assert(s.id >0L)
          assert(seasonList.size+1===seasonList1.size)
          assert(seasonList1.contains(s))
        }).unsafeRunSync()
      }

      it ("should find a season"){
        (for {
          _ <- Season.Dao.truncate().run.transact(transactor)
          s <- repo.insertSeason(season)
          s1<-repo.findSeason(s.id)
          sx<-repo.findSeason(-999L)
        } yield {
          assert(s1===Some(s))
          assert(sx.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a season"){
        (for {
          _ <- Season.Dao.truncate().run.transact(transactor)
          s <- repo.insertSeason(season)
          s1<-repo.findSeason(s.id)
          s2<-repo.updateSeason(s.copy(year=2001))
          s3<-repo.findSeason(s.id)
        } yield {
          assert(s1===Some(s))
          assert(s3===Some(s2))
          assert(!(s===s2))
        }).unsafeRunSync()

      }
      it("should delete a season"){
        (for {
          _ <- Season.Dao.truncate().run.transact(transactor)
          s <- repo.insertSeason(season)
          s1<-repo.findSeason(s.id)
          n<-repo.deleteSeason(s.id)
          s3<-repo.findSeason(s.id)
        } yield {
          assert(s1===Some(s))
          assert(n===1)
          assert(s3===None)
        }).unsafeRunSync()

      }


    }
    describe("Team ops"){

      it("should list teams") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          teamList <- repo.listTeam()
        } yield {
          assert(teamList.size === 0)
        }).unsafeRunSync()
      }

      it("should insert a team") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          teamList <- repo.listTeam()
          t <- repo.insertTeam(georgetown)
          teamList1 <- repo.listTeam()
        } yield {
          assert(t.id > 0L)
          assert(teamList.size + 1 === teamList1.size)
          assert(teamList1.contains(t))
        }).unsafeRunSync()
      }

      it("should find a team") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          t <- repo.insertTeam(georgetown)
          t1 <- repo.findTeam(t.id)
          tx <- repo.findTeam(-999L)
        } yield {
          assert(t1 === Some(t))
          assert(tx.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a team") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          t <- repo.insertTeam(georgetown)
          t1 <- repo.findTeam(t.id)
          t2 <- repo.updateTeam(t.copy(logoUrl = "logo.png"))
          t3 <- repo.findTeam(t.id)
        } yield {
          assert(t1 === Some(t))
          assert(t3 === Some(t2))
          assert(!(t === t2))
        }).unsafeRunSync()

      }
      it("should delete a team") {
        (for {
          _ <- Team.Dao.truncate().run.transact(transactor)
          t <- repo.insertTeam(georgetown)
          t1 <- repo.findTeam(t.id)
          n <- repo.deleteTeam(t.id)
          t3 <- repo.findTeam(t.id)
        } yield {
          assert(t1 === Some(t))
          assert(n === 1)
          assert(t3 === None)
        }).unsafeRunSync()
      }
    }
  }

}
