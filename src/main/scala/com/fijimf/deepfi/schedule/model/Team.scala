package com.fijimf.deepfi.schedule.model

import doobie.implicits._
import io.circe.generic.JsonCodec

final case class Team(id: Long, key: String, name: String, nickname: String, logoUrl: String, color1: String, color2: String) {
  def toSnapshotRecord(): TeamRecord = TeamRecord(key, name, nickname, logoUrl, color1, color2)
}

object Team {

  object Dao extends AbstractDao {
    val cols: Array[String] = Array("id", "key", "name", "nickname", "logo_url", "color1", "color2")
    val tableName = "team"

    def insert(t: Team): doobie.Update0 =
      (fr""" INSERT INTO team(key,name,nickname,logo_url,color1, color2)
             VALUES (${t.key}, ${t.name}, ${t.nickname}, ${t.logoUrl}, ${t.color1}, ${t.color2})
             RETURNING """ ++ colFr).update

    def update(t:Team): doobie.Update0 =
      (fr"""UPDATE team SET key=${t.key},  name=${t.name},  nickname=${t.nickname},  logo_url=${t.logoUrl},  color1=${t.color1},  color2=${t.color2}
            WHERE id=${t.id}
            RETURNING """++ colFr).update

    def find(id: Long): doobie.Query0[Team] = (baseQuery ++ fr" WHERE id = $id").query[Team]

    def findByKey(k: String): doobie.Query0[Team] = (baseQuery ++ fr" WHERE key = $k").query[Team]

    def findByAlias(k: String): doobie.Query0[Team] = {
      (prefixedQuery("team") ++ fr" INNER JOIN alias ON team.id = alias.team_id WHERE $k = alias.alias").query[Team]
    }

    def list(): doobie.Query0[Team] = baseQuery.query[Team]

    def delete(id: Long): doobie.Update0 =sql" DELETE FROM team where id=$id".update


  }

}