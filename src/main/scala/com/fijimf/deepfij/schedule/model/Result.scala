package com.fijimf.deepfij.schedule.model

import doobie.implicits._
import doobie.util.update.Update0
import io.circe.generic.JsonCodec

final case class Result(id: Long, gameId: Long, homeScore: Int, awayScore: Int, numPeriods: Int) {

}

object Result {

  object Dao extends AbstractDao {
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

    def list(): doobie.Query0[Result] = baseQuery.query[Result]

    def delete(id: Long): doobie.Update0 = sql"DELETE FROM result where id=$id".update

    def deleteByGameId(gameId: Long): doobie.Update0 = sql" DELETE FROM result where game_id=$gameId".update
  }

}
