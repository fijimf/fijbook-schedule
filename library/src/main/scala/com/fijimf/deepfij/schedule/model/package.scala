package com.fijimf.deepfij.schedule

import cats.Applicative
import cats.effect.Sync
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

package object model {
  implicit def intEntityDecoder[F[_] : Sync]: EntityDecoder[F, Int] = jsonOf
  implicit def intEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Int] = jsonEncoderOf
}
