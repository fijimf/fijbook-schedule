package com.fijimf.deepfij.schedule.model

import doobie.implicits._
import doobie.util.update.Update0

final case class Conference(id: Long, key: String, name: String, shortName:String, level: String, logoUrl: Option[String]) {
  def toSnapshotRecord(): ConferenceRecord = ConferenceRecord(key, name, shortName, level, logoUrl)
}

object Conference {

  object Dao extends AbstractDao {
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

}
