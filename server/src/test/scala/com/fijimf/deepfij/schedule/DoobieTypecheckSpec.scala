package com.fijimf.deepfij.schedule

import java.time.{LocalDate, LocalDateTime}

import com.fijimf.deepfij.schedule.model.Dao.{AliasDao, ConferenceDao, ConferenceMappingDao, GameDao, ResultDao, SeasonDao, TeamDao}
import com.fijimf.deepfij.schedule.model.{Alias, Conference, ConferenceMapping, Game, Result, Season, Team}

class DoobieTypecheckSpec extends DbIntegrationSpec {
  val containerName = "doobie-typecheck-spec"
  val port="7374"

  describe("Doobie typechecking Dao's") {
    describe("GameDao") {
      it("insert should typecheck") {
        check(GameDao.insert(Game(0L, 1L, LocalDate.now(), LocalDateTime.now(), 5L, 7L, Some("MCI Center"), None, "20110302")))
      }

      it("list should typecheck") {
        check(GameDao.list())
      }

      it("find should typecheck") {
        check(GameDao.find(99L))
      }

      it("findByLoadKey should typecheck") {
        check(GameDao.findByLoadKey("20190923"))
      }

      it("delete should typecheck") {
        check(GameDao.delete(99L))
      }

      it("update should typecheck") {
        check(GameDao.update(Game(83L, 1L, LocalDate.now(), LocalDateTime.now(), 5L, 7L, Some("MCI Center"), None, "20190911")))
      }

      it("truncate should typecheck") {
        check(GameDao.truncate())
      }
    }
    describe("ResultDao") {

      it("insert should typecheck") {
        check(ResultDao.insert(Result(0L, 1L, 92, 67, 2)))
      }

      it("list should typecheck") {
        check(ResultDao.list())
      }

      it("find should typecheck") {
        check(ResultDao.find(99L))
      }

      it("delete should typecheck") {
        check(ResultDao.delete(99L))
      }

      it("deleteByGameId should typecheck") {
        check(ResultDao.deleteByGameId(99L))
      }

      it("update should typecheck") {
        check(ResultDao.insert(Result(3L, 1L, 92, 67, 2)))
      }

      it("truncate should typecheck") {
        check(ResultDao.truncate())
      }
    }
    describe("SeasonDao") {

      it("insert should typecheck") {
        check(SeasonDao.insert(Season(0L, 1999)))
      }

      it("list should typecheck") {
        check(SeasonDao.list())
      }

      it("find should typecheck") {
        check(SeasonDao.find(99L))
      }

      it("findLatest should typecheck") {
        check(SeasonDao.findLatest())
      }

      it("findByDate should typecheck") {
        check(SeasonDao.findByDate(LocalDateTime.now()))
      }

      it("delete should typecheck") {
        check(SeasonDao.delete(99L))
      }

      it("update should typecheck") {
        check(SeasonDao.update(Season(3L, 1999)))
      }

      it("truncate should typecheck") {
        check(SeasonDao.truncate())
      }
    }
    describe("TeamDao") {

      it("insert should typecheck") {
        check(TeamDao.insert(Team(0L, "georgetown", "Georgetown", "Hoyas", "http://xxx.xxx.com/xxx/xxxxx/xxxxxxxx", "#AAFFDD", "blue")))
      }


      it("list should typecheck") {
        check(TeamDao.list())
      }

      it("find should typecheck") {
        check(TeamDao.find(99L))
      }
      it("findByAlias should typecheck") {
        check(TeamDao.findByAlias("southern-cal"))
      }

      it("findByKey should typecheck") {
        check(TeamDao.findByKey("usc"))
      }

      it("delete should typecheck") {
        check(TeamDao.delete(99L))
      }

      it("update should typecheck") {
        check(TeamDao.update(Team(0L, "georgetown", "Georgetown", "Hoyas", "http://xxx.xxx.com/xxx/xxxxx/xxxxxxxx", "#AAFFDD", "blue")))
      }

      it("truncate should typecheck") {
        check(TeamDao.truncate())
      }
    }
    describe("AliasDao") {

      it("insert should typecheck") {
        check(AliasDao.insert(Alias(0L, 2L, "st. johns")))
      }

      it("list should typecheck") {
        check(AliasDao.list())
      }

      it("find should typecheck") {
        check(AliasDao.find(99L))
      }

      it("delete should typecheck") {
        check(AliasDao.delete(99L))
      }

      it("update should typecheck") {
        check(AliasDao.update(Alias(3L, 4L, "ssssss")))
      }

      it("truncate should typecheck") {
        check(AliasDao.truncate())
      }

    }
    describe("ConferenceMappingDao") {

      it("insert should typecheck") {
        check(ConferenceMappingDao.insert(ConferenceMapping(0L, 2L, 3L, 4L)))
      }

      it("list should typecheck") {
        check(ConferenceMappingDao.list())
      }

      it("find should typecheck") {
        check(ConferenceMappingDao.find(99L))
      }

      it("delete should typecheck") {
        check(ConferenceMappingDao.delete(99L))
      }

      it("update should typecheck") {
        check(ConferenceMappingDao.update(ConferenceMapping(1L, 2L, 3L, 4L)))
      }

      it("truncate should typecheck") {
        check(ConferenceMappingDao.truncate())
      }
    }

    describe("ConferenceDao") {

      it("insert should typecheck") {
        check(ConferenceDao.insert(Conference(0L, "big-east", "Big East", "The Big East Conference","High Major", None)))
      }

      it("list should typecheck") {
        check(ConferenceDao.list())
      }

      it("find should typecheck") {
        check(ConferenceDao.find(99L))
      }

      it("delete should typecheck") {
        check(ConferenceDao.delete(99L))
      }

      it("update should typecheck") {
        check(ConferenceDao.update(Conference(1L, "big-east", "Big East", "The Big East Conference","High Major", None)))
      }

      it("truncate should typecheck") {
        check(ConferenceDao.truncate())
      }
    }
  }
}
