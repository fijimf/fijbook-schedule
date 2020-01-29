package com.fijimf.deepfij.schedule.model

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

final case class Season(id: Long, year: Int)

object Season {

  implicit val seasonEncoder: Encoder.AsObject[Season] = deriveEncoder[Season]
  implicit val seasonDecoder: Decoder[Season] = deriveDecoder[Season]

  implicit def seasonEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Season] = jsonEncoderOf

  implicit def lstSeasonEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Season]] = jsonEncoderOf

  implicit def lstSeasonEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Season]] = jsonOf

  implicit def seasonEntityDecoder[F[_] : Sync]: EntityDecoder[F, Season] = jsonOf

}
