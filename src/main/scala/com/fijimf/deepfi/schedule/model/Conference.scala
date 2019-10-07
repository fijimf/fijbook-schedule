package com.fijimf.deepfi.schedule.model

import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.update.Update0

final case class Conference(id: Long, key: String, name: String, longName: String, logoUrl: Option[String]) {

}

object Conference {

  object Dao extends AbstractDao {
    val cols: Array[String] = Array("id", "key", "name", "long_name", "logo_url")
    val tableName: String = "conference"
    def insert(c: Conference): Update0 =
      sql"""
    INSERT INTO conference(key,name,long_name,logo_url)
    VALUES (${c.key}, ${c.name}, ${c.longName}, ${c.logoUrl})
    RETURNING $colString """.update

    def find(id: Long): doobie.Query0[Conference] = (baseQuery ++
      fr"""
       WHERE id = $id
      """).query[Conference]

    def list(): doobie.Query0[Conference] = baseQuery.query[Conference]


    def delete(id: Long): doobie.Update0 =
      sql"""
        DELETE FROM conference where id=${id}
      """.update

    def update(c: Conference): Update0 =
      sql"""
        UPDATE conference set  key=${c.key}, name=${c.name}, long_name=${c.longName}, logo_url=${c.logoUrl}
        WHERE id= ${c.id}
         RETURNING $colString """.update
  }

}
