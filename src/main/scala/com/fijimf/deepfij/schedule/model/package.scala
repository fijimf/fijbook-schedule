package com.fijimf.deepfij.schedule

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto._
import io.circe.{Decoder, ObjectEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

package object model {
  implicit val aliasEncoder: ObjectEncoder[Alias] = deriveEncoder[Alias]
  implicit val aliasDecoder: Decoder[Alias] = deriveDecoder[Alias]
  implicit def aliasEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Alias] = jsonEncoderOf

  implicit def lstAliasEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Alias]] = jsonEncoderOf
  implicit def lstAliasEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Alias]] = jsonOf

  implicit def aliasEntityDecoder[F[_] : Sync]: EntityDecoder[F, Alias] = jsonOf

  implicit val conferenceEncoder: ObjectEncoder[Conference] = deriveEncoder[Conference]
  implicit val conferenceDecoder: Decoder[Conference] = deriveDecoder[Conference]

  implicit def conferenceEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Conference] = jsonEncoderOf

  implicit def lstConferenceEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Conference]] = jsonEncoderOf

  implicit def conferenceEntityDecoder[F[_] : Sync]: EntityDecoder[F, Conference] = jsonOf

  implicit val conferenceMappingEncoder: ObjectEncoder[ConferenceMapping] = deriveEncoder[ConferenceMapping]
  implicit val conferenceMappingDecoder: Decoder[ConferenceMapping] = deriveDecoder[ConferenceMapping]
  implicit def conferenceMappingEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ConferenceMapping] = jsonEncoderOf

  implicit def lstConferenceMappingEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[ConferenceMapping]] = jsonEncoderOf

  implicit def conferenceMappingEntityDecoder[F[_] : Sync]: EntityDecoder[F, ConferenceMapping] = jsonOf

  implicit val gameEncoder: ObjectEncoder[Game] = deriveEncoder[Game]
  implicit val gameDecoder: Decoder[Game] = deriveDecoder[Game]
  implicit def gameEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Game] = jsonEncoderOf

  implicit def lstGameEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Game]] = jsonEncoderOf

  implicit def gameEntityDecoder[F[_] : Sync]: EntityDecoder[F, Game] = jsonOf

  implicit val resultEncoder: ObjectEncoder[Result] = deriveEncoder[Result]
  implicit val resultDecoder: Decoder[Result] = deriveDecoder[Result]
  implicit def resultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Result] = jsonEncoderOf

  implicit def listResultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Result]] = jsonEncoderOf

  implicit def resultEntityDecoder[F[_] : Sync]: EntityDecoder[F, Result] = jsonOf

  implicit val seasonEncoder: ObjectEncoder[Season] = deriveEncoder[Season]
  implicit val seasonDecoder: Decoder[Season] = deriveDecoder[Season]
  implicit def seasonEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Season] = jsonEncoderOf

  implicit def lstSeasonEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Season]] = jsonEncoderOf

  implicit def seasonEntityDecoder[F[_] : Sync]: EntityDecoder[F, Season] = jsonOf

  implicit val teamEncoder: ObjectEncoder[Team] = deriveEncoder[Team]
  implicit val teamDecoder: Decoder[Team] = deriveDecoder[Team]
  implicit def teamEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Team] = jsonEncoderOf

  implicit def lstTeamEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Team]] = jsonEncoderOf

  implicit def teamEntityDecoder[F[_] : Sync]: EntityDecoder[F, Team] = jsonOf

  implicit def intEntityDecoder[F[_] : Sync]: EntityDecoder[F, Int] = jsonOf
}
