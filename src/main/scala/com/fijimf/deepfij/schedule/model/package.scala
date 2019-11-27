package com.fijimf.deepfij.schedule

import cats.Applicative
import cats.effect.Sync
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder, ObjectEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

package object model {
  implicit val aliasEncoder: Encoder.AsObject[Alias] = deriveEncoder[Alias]
  implicit val aliasDecoder: Decoder[Alias] = deriveDecoder[Alias]
  implicit def aliasEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Alias] = jsonEncoderOf

  implicit def lstAliasEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Alias]] = jsonEncoderOf
  implicit def lstAliasEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Alias]] = jsonOf

  implicit def aliasEntityDecoder[F[_] : Sync]: EntityDecoder[F, Alias] = jsonOf

  implicit val conferenceEncoder: Encoder.AsObject[Conference] = deriveEncoder[Conference]
  implicit val conferenceDecoder: Decoder[Conference] = deriveDecoder[Conference]

  implicit def conferenceEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Conference] = jsonEncoderOf

  implicit def lstConferenceEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Conference]] = jsonEncoderOf
  implicit def lstConferenceEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Conference]] = jsonOf

  implicit def conferenceEntityDecoder[F[_] : Sync]: EntityDecoder[F, Conference] = jsonOf

  implicit val conferenceMappingEncoder: Encoder.AsObject[ConferenceMapping] = deriveEncoder[ConferenceMapping]
  implicit val conferenceMappingDecoder: Decoder[ConferenceMapping] = deriveDecoder[ConferenceMapping]
  implicit def conferenceMappingEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ConferenceMapping] = jsonEncoderOf

  implicit def lstConferenceMappingEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[ConferenceMapping]] = jsonEncoderOf
  implicit def lstConferenceMappingEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[ConferenceMapping]] = jsonOf

  implicit def conferenceMappingEntityDecoder[F[_] : Sync]: EntityDecoder[F, ConferenceMapping] = jsonOf

  implicit val gameEncoder: Encoder.AsObject[Game] = deriveEncoder[Game]
  implicit val gameDecoder: Decoder[Game] = deriveDecoder[Game]
  implicit def gameEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Game] = jsonEncoderOf

  implicit def lstGameEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Game]] = jsonEncoderOf
  implicit def lstGameEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Game]] = jsonOf

  implicit def gameEntityDecoder[F[_] : Sync]: EntityDecoder[F, Game] = jsonOf

  implicit val resultEncoder: Encoder.AsObject[Result] = deriveEncoder[Result]
  implicit val resultDecoder: Decoder[Result] = deriveDecoder[Result]
  implicit def resultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Result] = jsonEncoderOf

  implicit def listResultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Result]] = jsonEncoderOf
  implicit def listResultEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Result]] = jsonOf

  implicit def resultEntityDecoder[F[_] : Sync]: EntityDecoder[F, Result] = jsonOf

  implicit val seasonEncoder: Encoder.AsObject[Season] = deriveEncoder[Season]
  implicit val seasonDecoder: Decoder[Season] = deriveDecoder[Season]
  implicit def seasonEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Season] = jsonEncoderOf

  implicit def lstSeasonEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Season]] = jsonEncoderOf
  implicit def lstSeasonEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Season]] = jsonOf

  implicit def seasonEntityDecoder[F[_] : Sync]: EntityDecoder[F, Season] = jsonOf

  implicit val teamEncoder: Encoder.AsObject[Team] = deriveEncoder[Team]
  implicit val teamDecoder: Decoder[Team] = deriveDecoder[Team]
  implicit def teamEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Team] = jsonEncoderOf

  implicit def lstTeamEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[Team]] = jsonEncoderOf
  implicit def lstTeamEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[Team]] = jsonOf

  implicit def teamEntityDecoder[F[_] : Sync]: EntityDecoder[F, Team] = jsonOf

  implicit def intEntityDecoder[F[_] : Sync]: EntityDecoder[F, Int] = jsonOf
  implicit def intEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Int] = jsonEncoderOf

  implicit val scheduleEncoder: Encoder.AsObject[Schedule] = deriveEncoder[Schedule]
  implicit val scheduleDecoder: Decoder[Schedule] = deriveDecoder[Schedule]
  implicit def scheduleEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Schedule] = jsonEncoderOf
  implicit def scheduleEntityDecoder[F[_] : Sync]: EntityDecoder[F, Schedule] = jsonOf

  implicit val scheduleRootEncoder: Encoder.AsObject[ScheduleRoot] = deriveEncoder[ScheduleRoot]
  implicit val scheduleRootDecoder: Decoder[ScheduleRoot] = deriveDecoder[ScheduleRoot]
  implicit def scheduleRootEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ScheduleRoot] = jsonEncoderOf
  implicit def scheduleRootEntityDecoder[F[_] : Sync]: EntityDecoder[F, ScheduleRoot] = jsonOf

  implicit val updateCandidateEncoder: Encoder.AsObject[UpdateCandidate] = deriveEncoder[UpdateCandidate]
  implicit val updateCandidateDecoder: Decoder[UpdateCandidate] = deriveDecoder[UpdateCandidate]
  implicit def updateCandidateEntityEncoder[F[_] : Applicative]: EntityEncoder[F, UpdateCandidate] = jsonEncoderOf
  implicit def updateCandidateEntityDecoder[F[_] : Sync]: EntityDecoder[F, UpdateCandidate] = jsonOf

  implicit def lstUpdateCandidateEntityEncoder[F[_] : Applicative]: EntityEncoder[F, List[UpdateCandidate]] = jsonEncoderOf
  implicit def lstUpdateCandidateEntityDecoder[F[_] : Sync]: EntityDecoder[F, List[UpdateCandidate]] = jsonOf

  implicit val scrapeResultEncoder: Encoder.AsObject[ScrapeResult] = deriveEncoder[ScrapeResult]
  implicit val scrapeResultDecoder: Decoder[ScrapeResult] = deriveDecoder[ScrapeResult]
  implicit def scrapeResultEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ScrapeResult] = jsonEncoderOf
  implicit def scrapeResultEntityDecoder[F[_] : Sync]: EntityDecoder[F, ScrapeResult] = jsonOf

}
