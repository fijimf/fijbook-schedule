package com.fijimf.deepfij.schedule.model

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonOf, jsonEncoderOf}


final case class Alias(id: Long, teamId: Long, alias: String)

object Alias {
  implicit val aliasEncoder: Encoder.AsObject[Alias] = deriveEncoder[Alias]
  implicit val aliasDecoder: Decoder[Alias] = deriveDecoder[Alias]
  implicit def aliasEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Alias] = jsonEncoderOf
  implicit def lstAliasEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Alias]] = jsonEncoderOf
  implicit def lstAliasEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Alias]] = jsonOf
  implicit def aliasEntityDecoder[F[_] : Sync]: EntityDecoder[F, Alias] = jsonOf
}
