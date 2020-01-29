package com.fijimf.deepfij.schedule.model

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

final case class Conference(id: Long, key: String, name: String, shortName: String, level: String, logoUrl: Option[String]) {
  def toSnapshotRecord: ConferenceRecord = ConferenceRecord(key, name, shortName, level, logoUrl)
}

object Conference {

  implicit val conferenceEncoder: Encoder.AsObject[Conference] = deriveEncoder[Conference]
  implicit val conferenceDecoder: Decoder[Conference] = deriveDecoder[Conference]

  implicit def conferenceEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Conference] = jsonEncoderOf

  implicit def lstConferenceEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Conference]] = jsonEncoderOf

  implicit def lstConferenceEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Conference]] = jsonOf

  implicit def conferenceEntityDecoder[F[_] : Sync]: EntityDecoder[F, Conference] = jsonOf

}
