package com.fijimf.deepfij.schedule.model

import java.time.LocalDate

import cats.effect.Sync
import cats.implicits._
import cats.{Applicative, Eq}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

case class ScheduleRoot
(
  teams: List[Team],
  conferences: List[Conference],
  seasons: List[Season],
  conferenceMappings: List[ConferenceMapping],
  games: List[Game],
  results: List[Result]
) {

  implicit val eqFoo: Eq[LocalDate] = Eq.fromUniversalEquals

  val schedules: List[Schedule] = seasons.map(seas => {
    val gs: List[Game] = games.filter(_.seasonId === seas.id)
    val gKeys = gs.map(_.id).toSet
    Schedule(
      seas,
      teams,
      conferences,
      conferenceMappings.filter(_.seasonId === seas.id),
      gs,
      results.filter(r => gKeys.contains(r.gameId)))
  })

  def dateToSeason(d:LocalDate):Option[Season] = {
    val y: Int = d.getMonthValue match {
      case m if m<6 => d.getYear
      case m if m>=6 => d.getYear+1
    }
    seasons.find(_.year===y).orElse(seasons.lastOption)
  }


  def currentSeason():Option[Season]=dateToSeason(today())

  def today():LocalDate = LocalDate.now()


  val seasonById: Map[Long, Season] = seasons.map(s => s.id -> s).toMap
  val seasonByYear: Map[Int, Season] = seasons.map(s => s.year -> s).toMap
  val gamesBySeason: Long => List[Game] = games.groupBy(_.seasonId).withDefaultValue(List.empty[Game])
}

object ScheduleRoot {

  implicit val scheduleRootEncoder: Encoder.AsObject[ScheduleRoot] = deriveEncoder[ScheduleRoot]
  implicit val scheduleRootDecoder: Decoder[ScheduleRoot] = deriveDecoder[ScheduleRoot]
  implicit def scheduleRootEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ScheduleRoot] = jsonEncoderOf
  implicit def scheduleRootEntityDecoder[F[_] : Sync]: EntityDecoder[F, ScheduleRoot] = jsonOf


}







