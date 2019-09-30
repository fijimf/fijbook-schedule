package com.fijimf.deepfi.schedule.model

import java.time.{LocalDate, LocalDateTime}

case class ProposedGame(dateTime:LocalDateTime, homeKey:String, awayKey:String, location:Option[String]) {
  val date: LocalDate = dateTime.toLocalDate
}



