package com.fijimf.deepfi.schedule.model

import java.time.{LocalDate, LocalDateTime}

final case class UpdateCandidate(dateTime: LocalDateTime, homeKey: String, awayKey: String, location: Option[String], isNeutral: Option[Boolean], homeScore: Option[Int], awayScore: Option[Int], numPeriods: Option[Int]) {
  val date: LocalDate = dateTime.toLocalDate

  def toGame(id: Long, seasonId: Long, homeId: Long, awayId: Long, loadKey: String): Game = Game(id, seasonId, date, dateTime, homeId, awayId, location, isNeutral, loadKey): Game

  def toOptionResult(id: Long, gameId: Long): Option[Result] = {
    (homeScore, awayScore) match {
      case (Some(h), Some(a)) => Some(Result(0L, 0L, h, a, numPeriods.getOrElse(2)))
      case _ => None
    }
  }
}
