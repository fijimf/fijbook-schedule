package com.fijimf.deepfij.schedule.model

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class Schedule(season: Season, teams: List[Team], conferences: List[Conference], conferenceMapping: List[ConferenceMapping], games: List[Game], results: List[Result])

object Schedule {
  implicit val scheduleEncoder: Encoder.AsObject[Schedule] = deriveEncoder[Schedule]
  implicit val scheduleDecoder: Decoder[Schedule] = deriveDecoder[Schedule]

  implicit def scheduleEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Schedule] = jsonEncoderOf

  implicit def scheduleEntityDecoder[F[_] : Sync]: EntityDecoder[F, Schedule] = jsonOf

}
