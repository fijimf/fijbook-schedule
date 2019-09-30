package com.fijimf.deepfi.schedule.model

import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.update.Update0

final case class Alias(id: Long, teamId: Long, alias: String) {

}

object Alias {

  object Dao {
    val cols: Array[String] = Array("id", "team_id", "alias")
    val colString: String = cols.mkString(", ")
    val baseQuery: Fragment = fr"""SELECT """ ++ Fragment.const(colString) ++ fr""" FROM alias """

    def insert(a: Alias): Update0 =
      sql"""
    INSERT INTO alias(team_id, alias) VALUES (${a.teamId}, ${a.alias})
    RETURNING $colString
    """.update

    def update(a:Alias):Update0 = {
      sql"""
            UPDATE alias SET team_id = ${a.teamId}, alias=${a.alias} WHERE id=${a.id}
            RETURNING $colString
            """.update
    }
    def find(id: Long): doobie.Query0[Alias] = (baseQuery ++
      fr"""
       WHERE id = $id
      """).query[Alias]

    def list(): doobie.Query0[Alias] = baseQuery.query[Alias]


    def delete(id: Long): doobie.Update0 =
      sql"""
        DELETE FROM alias where id=${id}
      """.update

  }

}
