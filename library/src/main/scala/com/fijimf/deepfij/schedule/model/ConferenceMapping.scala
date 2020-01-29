package com.fijimf.deepfij.schedule.model

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

final case class ConferenceMapping(id: Long, seasonId: Long, teamId: Long, conferenceId: Long)

object ConferenceMapping {


  implicit val conferenceMappingEncoder: Encoder.AsObject[ConferenceMapping] = deriveEncoder[ConferenceMapping]
  implicit val conferenceMappingDecoder: Decoder[ConferenceMapping] = deriveDecoder[ConferenceMapping]

  implicit def conferenceMappingEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ConferenceMapping] = jsonEncoderOf

  implicit def lstConferenceMappingEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[ConferenceMapping]] = jsonEncoderOf

  implicit def lstConferenceMappingEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[ConferenceMapping]] = jsonOf

  implicit def conferenceMappingEntityDecoder[F[_] : Sync]: EntityDecoder[F, ConferenceMapping] = jsonOf

}

