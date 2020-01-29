package com.fijimf.deepfij.schedule.model

import java.sql.Timestamp
import java.time.LocalDateTime

import com.fijimf.deepfij.schedule.model.Game.{MaybeResult, toGameAndOptionResult}
import doobie.implicits._
import doobie.util.Meta
import doobie.util.fragment.Fragment
import doobie.util.update.Update0

trait AbstractDao {
  def cols: Array[String]

  def tableName: String

  def colString: String = cols.mkString(", ")

  def colFr: Fragment = Fragment.const(colString)

  def baseQuery: Fragment = fr"""SELECT """ ++ Fragment.const(colString) ++ fr""" FROM """ ++ Fragment.const(tableName + " ")

  def prefixedCols(p: String): Array[String] = cols.map(s => p + "." + s)

  def prefixedQuery(p: String): Fragment = fr"""SELECT """ ++ Fragment.const(prefixedCols(p).mkString(",")) ++ fr""" FROM """ ++ Fragment.const(tableName + " " + p)

  def truncate(): Update0 = (fr"TRUNCATE " ++ Fragment.const(tableName) ++ fr" CASCADE").update
}

object Dao {

  object AliasDao extends AbstractDao {
    val cols: Array[String] = Array("id", "team_id", "alias")
    val tableName: String = "alias"

    def insert(a: Alias): Update0 =
      (fr"INSERT INTO alias(team_id, alias) VALUES (${a.teamId}, ${a.alias}) RETURNING" ++ colFr).update

    def update(a: Alias): Update0 =
      (fr""" UPDATE alias SET team_id = ${a.teamId}, alias=${a.alias} WHERE id=${a.id}
             RETURNING """ ++ colFr).update

    def find(id: Long): doobie.Query0[Alias] = (baseQuery ++ fr" WHERE id = $id").query[Alias]

    def list(): doobie.Query0[Alias] = baseQuery.query[Alias]

    def delete(id: Long): doobie.Update0 = sql"DELETE FROM alias where id=${id}".update

  }

  object ConferenceDao extends AbstractDao {
    val cols: Array[String] = Array("id", "key", "name", "short_name", "level", "logo_url")
    val tableName: String = "conference"

    def insert(c: Conference): Update0 =
      (fr"""INSERT INTO conference(key,name,short_name,level,logo_url)
             VALUES (${c.key}, ${c.name}, ${c.shortName}, ${c.level}, ${c.logoUrl})
             RETURNING """ ++ colFr).update

    def update(c: Conference): Update0 =
      (fr"""UPDATE conference set  key=${c.key}, name=${c.name}, short_name=${c.shortName}, level=${c.level}, logo_url=${c.logoUrl}
            WHERE id= ${c.id}
            RETURNING """ ++ colFr).update

    def find(id: Long): doobie.Query0[Conference] = (baseQuery ++ fr" WHERE id = $id").query[Conference]

    def list(): doobie.Query0[Conference] = baseQuery.query[Conference]

    def delete(id: Long): doobie.Update0 = sql"DELETE FROM conference where id=$id".update

  }

  object ConferenceMappingDao extends AbstractDao {

    val cols: Array[String] = Array("id", "season_id", "team_id", "conference_id")
    val tableName = "conference_mapping"

    def insert(cm: ConferenceMapping): Update0 =
      (fr""" INSERT INTO conference_mapping(season_id, team_id, conference_id)
             VALUES (${cm.seasonId}, ${cm.teamId}, ${cm.conferenceId})
             RETURNING """ ++ colFr).update

    def update(cm: ConferenceMapping): Update0 =
      (fr"""UPDATE conference_mapping SET season_id = ${cm.seasonId}, team_id = ${cm.teamId}, conference_id=${cm.conferenceId}
            WHERE id=${cm.id}
            RETURNING """ ++ colFr).update

    def find(id: Long): doobie.Query0[ConferenceMapping] = (baseQuery ++ fr" WHERE id = $id").query[ConferenceMapping]

    def findBySeason(seasonId: Long): doobie.Query0[ConferenceMapping] =
      (baseQuery ++ fr" WHERE season_id = $seasonId").query[ConferenceMapping]

    def list(): doobie.Query0[ConferenceMapping] = baseQuery.query[ConferenceMapping]

    def delete(id: Long): doobie.Update0 = sql" DELETE FROM conference_mapping where id=$id".update

  }


  object GameDao extends AbstractDao {

    implicit val localDateTimeMeta: Meta[LocalDateTime] = Meta[Timestamp].imap(ts => ts.toLocalDateTime)(ldt => Timestamp.valueOf(ldt))

    val cols: Array[String] = Array("id", "season_id", "date", "time", "home_team_id", "away_team_id", "location", "is_neutral", "load_key")
    val tableName = "game"

    def insert(g: Game): Update0 =
      (fr"""INSERT INTO game(season_id, date,time,home_team_id,away_team_id,location, is_neutral, load_key )
            VALUES (${g.seasonId},${g.date},${g.time},${g.homeTeamId},${g.awayTeamId},${g.location},${g.isNeutral},${g.loadKey})
            RETURNING """ ++ colFr).update

    def update(g: Game): Update0 =
      (fr"""UPDATE game SET season_id = ${g.seasonId}, date = ${g.date}, time = ${g.time}, home_team_id = ${g.homeTeamId}, away_team_id = ${g.awayTeamId}, location = ${g.location}, is_neutral = ${g.isNeutral}, load_key = ${g.loadKey}
            WHERE id=${g.id}
            RETURNING """ ++ colFr).update

    def find(id: Long): doobie.Query0[Game] = (baseQuery ++ fr" WHERE id = $id").query[Game]

    def findBySeason(id: Long): doobie.Query0[Game] = (baseQuery ++ fr" WHERE season_id = $id").query[Game]

    def findByLoadKey(loadKey: String): doobie.Query0[(Game, Option[Result])] =
      (fr"""SELECT """ ++
        Fragment.const((prefixedCols(tableName) ++ ResultDao.prefixedCols(ResultDao.tableName)).mkString(", ")) ++
        fr""" FROM """ ++
        Fragment.const(tableName + " ") ++
        fr"""LEFT OUTER JOIN result ON game.id = result.game_id""" ++
        fr"""WHERE load_key = ${loadKey}""").query[(Game, MaybeResult)].map(toGameAndOptionResult)

    def list(): doobie.Query0[Game] = baseQuery.query[Game]

    def delete(id: Long): doobie.Update0 = sql"DELETE FROM game where id=$id".update

    def deleteByLoadKey(loadKey: String): doobie.Update0 = sql"DELETE FROM game where load_key=$loadKey".update
  }

  object ResultDao extends AbstractDao {

    val cols: Array[String] = Array("id", "game_id", "home_score", "away_score", "num_periods")
    val tableName = "result"

    def insert(r: Result): Update0 =
      (fr"""INSERT INTO result(game_id, home_score, away_score, num_periods)
            VALUES (${r.gameId}, ${r.homeScore}, ${r.awayScore}, ${r.numPeriods})
            RETURNING """ ++ colFr).update

    def update(r: Result): doobie.Update0 =
      (fr""" UPDATE result SET game_id=${r.gameId},  home_score=${r.homeScore},  away_score=${r.awayScore},  num_periods =${r.numPeriods}
             WHERE id = ${r.id}
             RETURNING """ ++ colFr).update

    def find(id: Long): doobie.Query0[Result] = (baseQuery ++ fr" WHERE id = $id").query[Result]

    def findBySeason(seasonId: Long): doobie.Query0[Result] =
      (prefixedQuery("r") ++ fr" INNER JOIN game g ON r.game_id=g.id WHERE g.season_id = $seasonId").query[Result]

    def list(): doobie.Query0[Result] = baseQuery.query[Result]

    def delete(id: Long): doobie.Update0 = sql"DELETE FROM result where id=$id".update

    def deleteByGameId(gameId: Long): doobie.Update0 = sql" DELETE FROM result where game_id=$gameId".update
  }

  object SeasonDao extends AbstractDao {

    val cols: Array[String] = Array("id", "year")
    val tableName: String = "season"


    def insert(s: Season): Update0 = (fr"INSERT INTO season(year) VALUES (${s.year}) RETURNING " ++ colFr).update

    def find(id: Long): doobie.Query0[Season] = (baseQuery ++ fr" WHERE id = $id").query[Season]

    def findLatest(): doobie.Query0[Season] = (baseQuery ++ fr" ORDER BY year DESC LIMIT 1").query[Season]

    def findByYear(y: Int): doobie.Query0[Season] = (baseQuery ++ fr" WHERE year = $y").query[Season]

    def list(): doobie.Query0[Season] = baseQuery.query[Season]

    def delete(id: Long): doobie.Update0 = sql" DELETE FROM season where id=$id".update

    def update(s: Season): doobie.Update0 = (fr"UPDATE season SET year = ${s.year} WHERE id = ${s.id} RETURNING " ++ colFr).update

    // A date is in season yyyy id=f it is after 10/31/yyyy-1 and before 5/1/yyyy
    def findByDate(d: LocalDateTime): doobie.Query0[Season] = {
      val y: Int = d.getMonthValue match {
        case m if m < 5 => d.getYear
        case m if m > 10 => d.getYear + 1
        case _ => -1
      }
      (baseQuery ++ fr" WHERE year = $y").query[Season]
    }

  }


  object TeamDao extends AbstractDao {
    val cols: Array[String] = Array("id", "key", "name", "nickname", "logo_url", "color1", "color2")
    val tableName = "team"

    def insert(t: Team): doobie.Update0 =
      (fr""" INSERT INTO team(key,name,nickname,logo_url,color1, color2)
             VALUES (${t.key}, ${t.name}, ${t.nickname}, ${t.logoUrl}, ${t.color1}, ${t.color2})
             RETURNING """ ++ colFr).update

    def update(t: Team): doobie.Update0 =
      (fr"""UPDATE team SET key=${t.key},  name=${t.name},  nickname=${t.nickname},  logo_url=${t.logoUrl},  color1=${t.color1},  color2=${t.color2}
            WHERE id=${t.id}
            RETURNING """ ++ colFr).update

    def find(id: Long): doobie.Query0[Team] = (baseQuery ++ fr" WHERE id = $id").query[Team]

    def findByKey(k: String): doobie.Query0[Team] = (baseQuery ++ fr" WHERE key = $k").query[Team]

    def findByAlias(k: String): doobie.Query0[Team] = {
      (prefixedQuery("team") ++ fr" INNER JOIN alias ON team.id = alias.team_id WHERE $k = alias.alias").query[Team]
    }

    def list(): doobie.Query0[Team] = baseQuery.query[Team]

    def delete(id: Long): doobie.Update0 = sql" DELETE FROM team where id=$id".update


  }

}
