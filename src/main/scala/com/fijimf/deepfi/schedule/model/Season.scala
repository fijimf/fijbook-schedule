package com.fijimf.deepfi.schedule.model

import doobie.util.update.Update0
import doobie.implicits._
import doobie.util.fragment.Fragment

final case class Season(id: Long, year: Int) {

}

object Season {

  object Dao {
    val cols: Array[String] = Array("id", "year")
    val colString: String = cols.mkString(", ")
    val baseQuery: Fragment = fr"""SELECT """ ++ Fragment.const(colString) ++ fr""" FROM season """

    def insert(s: Season): Update0 = {
      sql"""
    INSERT INTO season(year)
    VALUES (${s.year})
    RETURNING $colString """.update
    }



    def find(id: Long): doobie.Query0[Season] = (baseQuery ++
      fr"""
       WHERE id = $id
      """).query[Season]

    def list(): doobie.Query0[Season] = baseQuery.query[Season]



    def delete(id: Long): doobie.Update0 =
      sql"""
        DELETE FROM season where id=${id}
      """.update

    def update(s:Season): doobie.Update0 = sql"""
      UPDATE season SET year = ${s.year} WHERE id = ${s.id}
      """.update
  }
}
