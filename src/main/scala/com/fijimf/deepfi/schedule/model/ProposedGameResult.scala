package com.fijimf.deepfi.schedule.model

import java.time.{LocalDate, LocalDateTime}

final case class ProposedGameResult(dateTime:LocalDateTime, homeKey:String, awayKey:String, location:Option[String], homeScore:Int, awayScore:Int, numPeriods:Option[Int]){
  val date: LocalDate = dateTime.toLocalDate
}
