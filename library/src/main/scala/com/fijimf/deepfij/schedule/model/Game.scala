package com.fijimf.deepfij.schedule.model

import java.time.{LocalDate, LocalDateTime}

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

final case class Game(id: Long, seasonId: Long, date: LocalDate, time: LocalDateTime, homeTeamId: Long, awayTeamId: Long, location: Option[String], isNeutral: Option[Boolean], loadKey: String)

object Game {
  type MaybeResult = (Option[Long], Option[Long], Option[Int], Option[Int], Option[Int])

  def toGameAndOptionResult(t: (Game, MaybeResult)): (Game, Option[Result]) = {
    val or: Option[Result] = t._2 match {
      case (Some(id), Some(g), Some(hs), Some(as), Some(np)) => Some(Result(id, g, hs, as, np))
      case _ => None
    }
    t._1 -> or
  }


  implicit val gameEncoder: Encoder.AsObject[Game] = deriveEncoder[Game]
  implicit val gameDecoder: Decoder[Game] = deriveDecoder[Game]

  implicit def gameEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Game] = jsonEncoderOf

  implicit def lstGameEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Game]] = jsonEncoderOf

  implicit def lstGameEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Game]] = jsonOf

  implicit def gameEntityDecoder[F[_] : Sync]: EntityDecoder[F, Game] = jsonOf


}
