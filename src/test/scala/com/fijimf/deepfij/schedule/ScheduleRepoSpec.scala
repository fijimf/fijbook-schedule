package com.fijimf.deepfij.schedule

import java.time.{LocalDate, LocalDateTime}

import cats.effect.IO
import com.fijimf.deepfi.schedule.model._
import com.fijimf.deepfi.schedule.services.ScheduleRepo

class ScheduleRepoSpec extends DbIntegrationSpec {
  val containerName = "schedule-repo-spec"
  val port="7375"

  describe("Schedule repo ops") {
    val repo = new ScheduleRepo[IO](transactor)
    describe("Alias ops") {
      it("should list all aliases") {
        (for {
          aliasList <- repo.listAliases()
        } yield {
          assert(aliasList.size >= 0)
        }).unsafeRunSync()
      }

      it("should insert an alias") {
        (for {
          aliasList0 <- repo.listAliases()
          a <- repo.insertAlias(Alias(0L, 32L, "st. johns"))
          aliasList1 <- repo.listAliases()
        } yield {
          assert(a.id > 0L)
          assert(aliasList1.size >= aliasList0.size)
          assert(aliasList1.contains(a))
        }).unsafeRunSync()
      }

      it("should find aliases") {
        (for {
          a <- repo.insertAlias(Alias(0L, 32L, "st. francis"))
          a1 <- repo.findAlias(a.id)
          ax <- repo.findAlias(-999L)
        } yield {
          assert(a1 === Some(a))
          assert(ax.isEmpty)
        }).unsafeRunSync()
      }

      it("should update an alias") {
        (for {
          a <- repo.insertAlias(Alias(0L, 32L, "usc"))
          a1 <- repo.findAlias(a.id)
          a2 <- repo.updateAlias(a.copy(alias = "southern cal"))
          a3 <- repo.findAlias(a.id)
        } yield {
          assert(a1 === Some(a))
          assert(!(a === a2))
          assert(a3 === Some(a2))
        }).unsafeRunSync()
      }

      it("should delete an alias") {
        (for {
          a <- repo.insertAlias(Alias(0L, 32L, "villanova"))
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
          conferenceList <- repo.listConferences()
        } yield {
          assert(conferenceList.size >= 0)
        }).unsafeRunSync()
      }

      it ("should insert a conference"){
        (for {
          conferenceList <- repo.listConferences()
          c<- repo.insertConference(Conference(0L, "big-east","Big East", "The Big East Conference", Some("http://Blah.blah.com/logo.png")))
          conferenceList1<-repo.listConferences()
        } yield {
          assert(c.id >0L)
          assert(conferenceList.size+1===conferenceList1.size)
          assert(conferenceList1.contains(c))
        }).unsafeRunSync()
      }

      it ("should find a conferences"){
        (for {
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
          cmList <- repo.listConferenceMappings()
        } yield {
          assert(cmList.size >= 0)
        }).unsafeRunSync()
      }

      it ("should insert a conferenceMapping"){
        (for {
          cmList <- repo.listConferenceMappings()
          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, 1L,2L,3L))
          cmList1<-repo.listConferenceMappings()
        } yield {
          assert(cm.id >0L)
          assert(cmList.size+1===cmList1.size)
          assert(cmList1.contains(cm))
        }).unsafeRunSync()
      }

      it ("should find a conferenceMapping"){
        (for {
          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, 1L,3L,3L))
          cm1<-repo.findConferenceMapping(cm.id)
          cmx<-repo.findConferenceMapping(-999L)
        } yield {
          assert(cm1===Some(cm))
          assert(cmx.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a conferenceMapping"){
        (for {
          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, 1L,4L,3L))
          cm1<-repo.findConferenceMapping(cm.id)
          cm2<-repo.updateConferenceMapping(cm.copy(conferenceId=5L))
          cm3<-repo.findConferenceMapping(cm.id)
        } yield {
          assert(cm1===Some(cm))
          assert(cm3===Some(cm2))
          assert(!(cm===cm2))
        }).unsafeRunSync()
      }

      it("should delete a conferenceMapping"){
        (for {
          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, 1L,5L,3L))
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

    }
    describe("Result ops"){

    }
    describe("Season ops"){

      it("should list seasons"){
        (for {
          seasonList <- repo.listSeason()
        } yield {
          assert(seasonList.size >= 0)
        }).unsafeRunSync()
      }

      it ("should insert a season"){
        (for {
          seasonList <- repo.listSeason()
          s<- repo.insertSeason(Season(0L, 2014))
          seasonList1<-repo.listSeason()
        } yield {
          assert(s.id >0L)
          assert(seasonList.size+1===seasonList1.size)
          assert(seasonList1.contains(s))
        }).unsafeRunSync()
      }

      it ("should find a season"){
        (for {
          s<- repo.insertSeason(Season(0L, 2013))
          s1<-repo.findSeason(s.id)
          sx<-repo.findSeason(-999L)
        } yield {
          assert(s1===Some(s))
          assert(sx.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a season"){
        (for {
          s<- repo.insertSeason(Season(0L, 2012))
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
          s<- repo.insertSeason(Season(0L, 2011))
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

    }
  }

}
