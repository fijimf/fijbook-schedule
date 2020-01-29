package com.fijimf.deepfij.schedule.model

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

final case class Team(id: Long, key: String, name: String, nickname: String, logoUrl: String, color1: String, color2: String) {
  def toSnapshotRecord: TeamRecord = TeamRecord(key, name, nickname, logoUrl, color1, color2)
}

object Team {

  implicit val teamEncoder: Encoder.AsObject[Team] = deriveEncoder[Team]
  implicit val teamDecoder: Decoder[Team] = deriveDecoder[Team]

  implicit def teamEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Team] = jsonEncoderOf

  implicit def lstTeamEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Team]] = jsonEncoderOf

  implicit def lstTeamEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Team]] = jsonOf

  implicit def teamEntityDecoder[F[_] : Sync]: EntityDecoder[F, Team] = jsonOf


}