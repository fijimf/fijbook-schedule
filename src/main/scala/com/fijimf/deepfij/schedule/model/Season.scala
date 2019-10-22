package com.fijimf.deepfij.schedule.model

import java.time.LocalDateTime

import doobie.implicits._
import doobie.util.update.Update0

final case class Season(id: Long, year: Int) {

}

object Season {

  object Dao extends AbstractDao {
    val cols: Array[String] = Array("id", "year")
    val tableName: String = "season"

    def insert(s: Season): Update0 = (fr"INSERT INTO season(year) VALUES (${s.year}) RETURNING "++colFr).update

    def find(id: Long): doobie.Query0[Season] = (baseQuery ++fr" WHERE id = $id").query[Season]

    def list(): doobie.Query0[Season] = baseQuery.query[Season]

    def delete(id: Long): doobie.Update0 =sql" DELETE FROM season where id=$id".update

    def update(s:Season): doobie.Update0 = (fr"UPDATE season SET year = ${s.year} WHERE id = ${s.id} RETURNING "++colFr).update

    // A date is in season yyyy id=f it is after 10/31/yyyy-1 and before 5/1/yyyy
    def findByDate(d: LocalDateTime): doobie.Query0[Season] = {
      val y: Int = d.getMonthValue match {
        case m if m < 5 => d.getYear
        case m if m > 10 => d.getYear + 1
        case _ => -1
      }
      (baseQuery ++fr" WHERE year = $y").query[Season]
    }

  }
}
