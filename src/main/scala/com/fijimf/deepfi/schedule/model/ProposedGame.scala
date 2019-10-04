package com.fijimf.deepfi.schedule.model

import java.time.{LocalDate, LocalDateTime}

case class ProposedGame(dateTime:LocalDateTime, homeKey:String, awayKey:String, location:Option[String], isNeutral:Option[Boolean]) {
  val date: LocalDate = dateTime.toLocalDate
}



