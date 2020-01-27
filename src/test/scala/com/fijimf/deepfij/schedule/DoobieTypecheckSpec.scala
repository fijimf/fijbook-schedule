package com.fijimf.deepfij.schedule

import java.time.{LocalDate, LocalDateTime}

import com.fijimf.deepfij.schedule.model.{Alias, Conference, ConferenceMapping, Game, Result, Season, Team}

class DoobieTypecheckSpec extends DbIntegrationSpec {
  val containerName = "doobie-typecheck-spec"
  val port="7374"

  describe("Doobie typechecking Dao's") {
    describe("Game.Dao") {
      it("insert should typecheck") {
        check(Game.Dao.insert(Game(0L, 1L, LocalDate.now(), LocalDateTime.now(), 5L, 7L, Some("MCI Center"), None, "20110302")))
      }

      it("list should typecheck") {
        check(Game.Dao.list())
      }

      it("find should typecheck") {
        check(Game.Dao.find(99L))
      }

      it("findByLoadKey should typecheck") {
        check(Game.Dao.findByLoadKey("20190923"))
      }

      it("delete should typecheck") {
        check(Game.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Game.Dao.update(Game(83L, 1L, LocalDate.now(), LocalDateTime.now(), 5L, 7L, Some("MCI Center"), None, "20190911")))
      }

      it("truncate should typecheck") {
        check(Game.Dao.truncate())
      }
    }
    describe("Result.Dao") {

      it("insert should typecheck") {
        check(Result.Dao.insert(Result(0L, 1L, 92, 67, 2)))
      }

      it("list should typecheck") {
        check(Result.Dao.list())
      }

      it("find should typecheck") {
        check(Result.Dao.find(99L))
      }

      it("delete should typecheck") {
        check(Result.Dao.delete(99L))
      }

      it("deleteByGameId should typecheck") {
        check(Result.Dao.deleteByGameId(99L))
      }

      it("update should typecheck") {
        check(Result.Dao.insert(Result(3L, 1L, 92, 67, 2)))
      }

      it("truncate should typecheck") {
        check(Result.Dao.truncate())
      }
    }
    describe("Season.Dao") {

      it("insert should typecheck") {
        check(Season.Dao.insert(Season(0L, 1999)))
      }

      it("list should typecheck") {
        check(Season.Dao.list())
      }

      it("find should typecheck") {
        check(Season.Dao.find(99L))
      }

      it("findLatest should typecheck") {
        check(Season.Dao.findLatest())
      }

      it("findByDate should typecheck") {
        check(Season.Dao.findByDate(LocalDateTime.now()))
      }

      it("delete should typecheck") {
        check(Season.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Season.Dao.update(Season(3L, 1999)))
      }

      it("truncate should typecheck") {
        check(Season.Dao.truncate())
      }
    }
    describe("Team.Dao") {

      it("insert should typecheck") {
        check(Team.Dao.insert(Team(0L, "georgetown", "Georgetown", "Hoyas", "http://xxx.xxx.com/xxx/xxxxx/xxxxxxxx", "#AAFFDD", "blue")))
      }


      it("list should typecheck") {
        check(Team.Dao.list())
      }

      it("find should typecheck") {
        check(Team.Dao.find(99L))
      }
      it("findByAlias should typecheck") {
        check(Team.Dao.findByAlias("southern-cal"))
      }

      it("findByKey should typecheck") {
        check(Team.Dao.findByKey("usc"))
      }

      it("delete should typecheck") {
        check(Team.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Team.Dao.update(Team(0L, "georgetown", "Georgetown", "Hoyas", "http://xxx.xxx.com/xxx/xxxxx/xxxxxxxx", "#AAFFDD", "blue")))
      }

      it("truncate should typecheck") {
        check(Team.Dao.truncate())
      }
    }
    describe("Alias.Dao") {

      it("insert should typecheck") {
        check(Alias.Dao.insert(Alias(0L, 2L, "st. johns")))
      }

      it("list should typecheck") {
        check(Alias.Dao.list())
      }

      it("find should typecheck") {
        check(Alias.Dao.find(99L))
      }

      it("delete should typecheck") {
        check(Alias.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Alias.Dao.update(Alias(3L, 4L, "ssssss")))
      }

      it("truncate should typecheck") {
        check(Alias.Dao.truncate())
      }

    }
    describe("ConferenceMapping.Dao") {

      it("insert should typecheck") {
        check(ConferenceMapping.Dao.insert(ConferenceMapping(0L, 2L, 3L, 4L)))
      }

      it("list should typecheck") {
        check(ConferenceMapping.Dao.list())
      }

      it("find should typecheck") {
        check(ConferenceMapping.Dao.find(99L))
      }

      it("delete should typecheck") {
        check(ConferenceMapping.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(ConferenceMapping.Dao.update(ConferenceMapping(1L, 2L, 3L, 4L)))
      }

      it("truncate should typecheck") {
        check(ConferenceMapping.Dao.truncate())
      }
    }

    describe("Conference.Dao") {

      it("insert should typecheck") {
        check(Conference.Dao.insert(Conference(0L, "big-east", "Big East", "The Big East Conference","High Major", None)))
      }

      it("list should typecheck") {
        check(Conference.Dao.list())
      }

      it("find should typecheck") {
        check(Conference.Dao.find(99L))
      }

      it("delete should typecheck") {
        check(Conference.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Conference.Dao.update(Conference(1L, "big-east", "Big East", "The Big East Conference","High Major", None)))
      }

      it("truncate should typecheck") {
        check(Conference.Dao.truncate())
      }
    }
  }
}
