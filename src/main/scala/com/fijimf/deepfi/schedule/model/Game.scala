package com.fijimf.deepfi.schedule.model

import java.sql.Timestamp
import java.time.{LocalDate, LocalDateTime}

import doobie.implicits._
import doobie.util.Meta
import doobie.util.fragment.Fragment
import doobie.util.update.Update0

final case class Game(id: Long, seasonId: Long, date: LocalDate, time: LocalDateTime, homeTeamId: Long, awayTeamId: Long, location: Option[String], isNeutral: Option[Boolean], loadKey: String) {


}

object Game {
  type MaybeResult = (Option[Long], Option[Long], Option[Int], Option[Int], Option[Int])

  def toGameAndOptionResult(t: (Game, MaybeResult)): (Game, Option[Result]) = {
    val or: Option[Result] = t._2 match {
      case (Some(id), Some(g), Some(hs), Some(as), Some(np)) => Some(Result(id, g, hs, as, np))
      case _ => None
    }
    t._1 -> or
  }

  object Dao extends AbstractDao {
    implicit val localDateTimeMeta: Meta[LocalDateTime] = Meta[Timestamp].imap(ts => ts.toLocalDateTime)(ldt => Timestamp.valueOf(ldt))

    val cols: Array[String] = Array("id", "season_id", "date", "time", "home_team_id", "away_team_id", "location", "is_neutral", "load_key")
   val tableName= "game"
    def insert(g: Game): Update0 =
      sql"""
    INSERT INTO game(season_id, date,time,home_team_id,away_team_id,location, is_neutral, load_key )
    VALUES (${g.seasonId},${g.date},${g.time},${g.homeTeamId},${g.awayTeamId},${g.location},${g.isNeutral},${g.loadKey})
    RETURNING $colString """.update


    def find(id: Long): doobie.Query0[Game] = (baseQuery ++
      fr"""
       WHERE id = $id
      """).query[Game]

    def findByLoadKey(loadKey: String): doobie.Query0[(Game, Option[Result])] =
      (fr"""SELECT """ ++
        Fragment.const((prefixedCols(tableName) ++ Result.Dao.prefixedCols(Result.Dao.tableName)).mkString(", ")) ++
        fr""" FROM """ ++
        Fragment.const(tableName + " ") ++
        fr"""LEFT OUTER JOIN result ON game.id = result.game_id""" ++
        fr"""WHERE load_key = ${loadKey}""").query[(Game, MaybeResult)].map(toGameAndOptionResult)

    def list(): doobie.Query0[Game] = baseQuery.query[Game]


    def delete(id: Long): doobie.Update0 =
      sql"""
        DELETE FROM game where id=$id
      """.update

    def deleteByLoadKey(loadKey: String): doobie.Update0 =
      sql"""
        DELETE FROM game where load_key=$loadKey
      """.update


    def update(g: Game): Update0 =
      sql"""
      UPDATE game SET season_id = ${g.seasonId}, date = ${g.date}, time = ${g.time}, home_team_id = ${g.homeTeamId}, away_team_id = ${g.awayTeamId}, location = ${g.location}, is_neutral = ${g.isNeutral}, load_key = ${g.loadKey}
      WHERE id=${g.id}
      """.update
  }

}
