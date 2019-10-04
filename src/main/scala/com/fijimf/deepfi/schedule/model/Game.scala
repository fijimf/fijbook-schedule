package com.fijimf.deepfi.schedule.model

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}

import doobie.implicits._
import doobie.util.Meta
import doobie.util.fragment.Fragment
import doobie.util.update.Update0

final case class Game(id: Long, seasonId: Long, date: LocalDate, time: LocalDateTime, homeTeamId: Long, awayTeamId: Long, location: Option[String], isNeutral: Option[Boolean]) {

}

object Game {

  object Dao {
    implicit val localDateTimeMeta: Meta[LocalDateTime] = Meta[Timestamp].imap(ts => ts.toLocalDateTime)(ldt => Timestamp.valueOf(ldt))

    val cols: Array[String] = Array("id", "season_id", "date", "time", "home_team_id", "away_team_id", "location", "is_neutral")
    val colString: String = cols.mkString(", ")
    val baseQuery: Fragment = fr"""SELECT """ ++ Fragment.const(colString) ++ fr""" FROM game """

    def insert(g: Game): Update0 =
      sql"""
    INSERT INTO game(season_id, date,time,home_team_id,away_team_id,location, is_neutral )
    VALUES (${g.seasonId},${g.date},${g.time},${g.homeTeamId},${g.awayTeamId},${g.location},${g.isNeutral})
    RETURNING $colString """.update


    def find(id: Long): doobie.Query0[Game] = (baseQuery ++
      fr"""
       WHERE id = $id
      """).query[Game]

    def findByDateTeams(date: LocalDate, homeTeamId:Long, awayTeamId: Long): doobie.Query0[Game] = (baseQuery ++
      fr"""
       WHERE date = $date and home_team_id=${homeTeamId} and away_team_id=${awayTeamId}
      """).query[Game]

    def list(): doobie.Query0[Game] = baseQuery.query[Game]


    def delete(id: Long): doobie.Update0 =
      sql"""
        DELETE FROM game where id=${id}
      """.update


    def update(g: Game): Update0 =
      sql"""
      UPDATE game SET season_id = ${g.seasonId}, date = ${g.date}, time = ${g.time}, home_team_id = ${g.homeTeamId}, away_team_id = ${g.awayTeamId}, location = ${g.location}, is_neutral = ${g.isNeutral}
      WHERE id=${g.id}
      """.update
  }

}
