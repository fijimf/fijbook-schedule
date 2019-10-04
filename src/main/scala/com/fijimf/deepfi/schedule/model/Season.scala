package com.fijimf.deepfi.schedule.model

import java.time.LocalDateTime

import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.update.Update0

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

    // A date is in season yyyy id=f it is after 10/31/yyyy-1 and before 5/1/yyyy
    def findByDate(d: LocalDateTime): doobie.Query0[Season] = {
      val y = d.getMonthValue match {
        case m if m < 5 => d.getYear
        case m if m > 10 => d.getYear + 1
        case _ => -1
      }
      (baseQuery ++
        fr"""
       WHERE year = $y
      """).query[Season]
    }

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
