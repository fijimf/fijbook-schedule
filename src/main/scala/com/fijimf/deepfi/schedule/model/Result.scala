package com.fijimf.deepfi.schedule.model

import com.fijimf.deepfi.schedule.model.Conference.Dao.colString
import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.update.Update0

final case class Result(id: Long, gameId: Long, homeScore: Int, awayScore: Int, numPeriods: Int) {

}

object Result {

  object Dao {
    val cols: Array[String] = Array("id", "game_id", "home_score", "away_score", "num_periods")
    val colString: String = cols.mkString(", ")
    val baseQuery: Fragment = fr"""SELECT """ ++ Fragment.const(colString) ++ fr""" FROM result """

    def insert(r: Result): Update0 =
      sql"""
    INSERT INTO result(game_id, home_score, away_score, num_periods)
    VALUES (${r.gameId}, ${r.homeScore}, ${r.awayScore}, ${r.numPeriods})
    RETURNING $colString """.update




    def find(id: Long): doobie.Query0[Result] = (baseQuery ++
      fr"""
       WHERE id = $id
      """).query[Result]

    def list(): doobie.Query0[Result] = baseQuery.query[Result]





    def delete(id: Long): doobie.Update0 =
      sql"""
        DELETE FROM result where id=${id}
      """.update

    def update(r:Result): doobie.Update0 = sql"""
      UPDATE result SET game_id=${r.gameId},  home_score=${r.homeScore},  away_score=${r.awayScore},  num_periods =${r.numPeriods}
      WHERE id = ${r.id}
      """.update
  }

}
