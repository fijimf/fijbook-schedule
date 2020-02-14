package com.fijimf.deepfij.schedule.model

import java.time.LocalDate

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Eq}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.apache.commons.codec.digest.DigestUtils
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class Schedule(season: Season, teams: List[Team], conferences: List[Conference], conferenceMapping: List[ConferenceMapping], games: List[Game], results: List[Result]) {
  implicit val eqFoo: Eq[LocalDate] = Eq.fromUniversalEquals

  val teamById: Map[Long, Team] = teams.map(t => t.id -> t).toMap
  val teamByKey: Map[String, Team] = teams.map(t => t.key -> t).toMap
  val conferenceById: Map[Long, Conference] = conferences.map(c => c.id -> c).toMap
  val conferenceByKey: Map[String, Conference] = conferences.map(c => c.key -> c).toMap
  val resultsByGame: Map[Long, Result] = results.map(r => r.gameId -> r).toMap

  def digest:String = DigestUtils.md5Hex(
    season.toString +
      teams.map(_.toString).mkString(",") +
      conferences.map(_.toString).mkString(",") +
      conferenceMapping.map(_.toString).mkString(",") +
      games.map(_.toString).mkString(",") +
      results.map(_.toString).mkString(",")
  )

  def gamesForTeam(t: Team): List[Game] = {
    games.filter(g => isPlaying(t, g))
  }

  def gamesForTeamWithResults(t: Team): List[(Game, Option[Result])] = {
    withResults(gamesForTeam(t))
  }

  def withResults(gs: List[Game]): List[(Game, Option[Result])] = gs.map(g => g -> resultsByGame.get(g.id))

  def teamsByConference(c: Conference): List[Team] = {
    conferenceMapping.filter(_.conferenceId == c.id).map(cm => teamById(cm.teamId))
  }

  def conferenceStandings(c: Conference): ConferenceStandings = {
    ConferenceStandings(c, teamsByConference(c).map(t => {
      val teamGames: List[(Game, Option[Result])] = withResults(gamesForTeam(t))
      val confRecord: WonLossRecord = WonLossRecord.from(t, teamGames.filter(q => isConferenceGame(q._1)))
      val overallRecord: WonLossRecord = WonLossRecord.from(t, teamGames)
      StandingsRow(t, confRecord, overallRecord)
    }))
  }

  def isConferenceGame(g: Game): Boolean = {
    (for {
      c1 <- conferenceMapping.find(c => c.teamId === g.homeTeamId)
      c2 <- conferenceMapping.find(c => c.teamId === g.awayTeamId)
    } yield {
      c1.conferenceId === c2.conferenceId
    }).getOrElse(false)
  }

  def isPlaying(t: Team, g: Game): Boolean = g.homeTeamId === t.id || g.awayTeamId === t.id

  def isWinner(t: Team, g: Game, r: Option[Result]): Option[Boolean] = r match {
    case Some(r) if r.homeScore > r.awayScore => Some(t.id === g.homeTeamId)
    case Some(r) if r.homeScore < r.awayScore => Some(t.id === g.awayTeamId)
    case None => None
  }

  def isLoser(t: Team, g: Game, r: Option[Result]): Option[Boolean] = r match {
    case Some(r) if r.homeScore < r.awayScore => Some(t.id === g.homeTeamId)
    case Some(r) if r.homeScore > r.awayScore => Some(t.id === g.awayTeamId)
    case None => None
  }

  def gamesForDate(date: LocalDate): List[Game] = {
    games.filter(_.date === date)
  }

  def gamesForDateWithResults(date: LocalDate): List[(Game, Option[Result])] = withResults(gamesForDate(date))

  def gameDates: List[LocalDate] =games.map(_.date).distinct.sortBy(_.toEpochDay)

  def seasonDates: List[LocalDate] = (for {
    minDate <- gameDates.headOption
    maxDate <- gameDates.lastOption
  } yield {
    Stream.iterate(minDate)(d => d.plusDays(1)).takeWhile(_.isBefore(maxDate.plusDays(1))).toList
  }).getOrElse(List.empty[LocalDate])

  def scoreboard(d:LocalDate): List[(Game, Result)] ={
    gamesForDateWithResults(d).map{
      case (g, Some(r))=>Some(g->r)
      case (g, None)=>None
    }.flatten
  }
}

object Schedule {
  implicit val scheduleEncoder: Encoder.AsObject[Schedule] = deriveEncoder[Schedule]
  implicit val scheduleDecoder: Decoder[Schedule] = deriveDecoder[Schedule]

  implicit def scheduleEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Schedule] = jsonEncoderOf

  implicit def scheduleEntityDecoder[F[_] : Sync]: EntityDecoder[F, Schedule] = jsonOf

}
