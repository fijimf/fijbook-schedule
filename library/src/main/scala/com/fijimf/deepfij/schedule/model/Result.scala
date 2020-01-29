package com.fijimf.deepfij.schedule.model

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

final case class Result(id: Long, gameId: Long, homeScore: Int, awayScore: Int, numPeriods: Int)

object Result {

  implicit val resultEncoder: Encoder.AsObject[Result] = deriveEncoder[Result]
  implicit val resultDecoder: Decoder[Result] = deriveDecoder[Result]

  implicit def resultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Result] = jsonEncoderOf

  implicit def listResultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Result]] = jsonEncoderOf

  implicit def listResultEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Result]] = jsonOf

  implicit def resultEntityDecoder[F[_] : Sync]: EntityDecoder[F, Result] = jsonOf

}
