package com.fijimf.deepfij.schedule.model

import java.time.{LocalDate, LocalDateTime}

import org.http4s.circe.{jsonEncoderOf, jsonOf}

import cats.Applicative
import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}

final case class UpdateCandidate(dateTime: LocalDateTime, homeKey: String, awayKey: String, location: Option[String], isNeutral: Option[Boolean], homeScore: Option[Int], awayScore: Option[Int], numPeriods: Option[Int]) {
  val date: LocalDate = dateTime.toLocalDate

  def toGame(id: Long, key: GameKey, loadKey: String): Game = Game(id, key.seasonId, date, dateTime, key.homeTeamId, key.awayTeamId, location, isNeutral, loadKey)

  def toOptionResult(id: Long, gameId: Long): Option[Result] = {
    (homeScore, awayScore) match {
      case (Some(h), Some(a)) => Some(Result(0L, 0L, h, a, numPeriods.getOrElse(2)))
      case _ => None
    }
  }
}

object UpdateCandidate {
  implicit val updateCandidateEncoder: Encoder.AsObject[UpdateCandidate] = deriveEncoder[UpdateCandidate]
  implicit val updateCandidateDecoder: Decoder[UpdateCandidate] = deriveDecoder[UpdateCandidate]
  implicit def updateCandidateEntityEncoder[F[_] : Applicative]: EntityEncoder[F, UpdateCandidate] = jsonEncoderOf
  implicit def updateCandidateEntityDecoder[F[_] : Sync]: EntityDecoder[F, UpdateCandidate] = jsonOf

  implicit def lstUpdateCandidateEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[UpdateCandidate]] = jsonEncoderOf
  implicit def lstUpdateCandidateEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[UpdateCandidate]] = jsonOf

}
