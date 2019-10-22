package com.fijimf.deepfij.schedule.model

import doobie.implicits._
import doobie.util.update.Update0

final case class Alias(id: Long, teamId: Long, alias: String) {

}

object Alias {

  object Dao extends AbstractDao {
    val cols: Array[String] = Array("id", "team_id", "alias")
    val tableName: String = "alias"

    def insert(a: Alias): Update0 =
      (fr"INSERT INTO alias(team_id, alias) VALUES (${a.teamId}, ${a.alias}) RETURNING" ++ colFr).update

    def update(a:Alias):Update0 =
      (fr""" UPDATE alias SET team_id = ${a.teamId}, alias=${a.alias} WHERE id=${a.id}
             RETURNING """ ++colFr).update

    def find(id: Long): doobie.Query0[Alias] = (baseQuery ++ fr" WHERE id = $id" ).query[Alias]

    def list(): doobie.Query0[Alias] = baseQuery.query[Alias]

    def delete(id: Long): doobie.Update0 = sql"DELETE FROM alias where id=${id}".update

  }

}
