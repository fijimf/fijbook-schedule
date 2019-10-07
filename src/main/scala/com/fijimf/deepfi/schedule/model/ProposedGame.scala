package com.fijimf.deepfi.schedule.model

import java.time.{LocalDate, LocalDateTime}

final case class ProposedGame(dateTime:LocalDateTime, homeKey:String, awayKey:String, location:Option[String], isNeutral:Option[Boolean]) {
  val date: LocalDate = dateTime.toLocalDate

  def toGame(id: Long, seasonId: Long, homeId: Long, awayId: Long, loadKey: String): Game = Game(id, seasonId, date, dateTime, homeId, awayId, location, isNeutral, loadKey):Game
}



