package com.fijimf.deepfij.schedule.model
import cats.implicits._

case class StandingsRow(t: Team, conferenceRecord: WonLossRecord, overallRecord: WonLossRecord) extends Ordered[StandingsRow] {
  override def compare(r: StandingsRow): Int = {
    val a: Int = conferenceRecord.compare(r.conferenceRecord)
    val b: Int = overallRecord.compare(r.overallRecord)
    val c: Int = t.name.compare(r.t.name)
    if (a === 0) {
      if (b === 0) {
        c
      } else {
        b
      }
    } else {
      a
    }
  }
}
