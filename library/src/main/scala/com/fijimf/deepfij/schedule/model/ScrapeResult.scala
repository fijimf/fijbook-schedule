package com.fijimf.deepfij.schedule.model

import cats.Applicative
import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}


case class ScrapeResult(loadKey:String, updates:List[UpdateCandidate])

object ScrapeResult {

  implicit val scrapeResultEncoder: Encoder.AsObject[ScrapeResult] = deriveEncoder[ScrapeResult]
  implicit val scrapeResultDecoder: Decoder[ScrapeResult] = deriveDecoder[ScrapeResult]
  implicit def scrapeResultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ScrapeResult] = jsonEncoderOf
  implicit def scrapeResultEntityDecoder[F[_] : Sync]: EntityDecoder[F, ScrapeResult] = jsonOf

}
