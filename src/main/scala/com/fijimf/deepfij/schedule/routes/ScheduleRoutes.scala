package com.fijimf.deepfij.schedule.routes

import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.model._
import com.fijimf.deepfij.schedule.services.ScheduleRepo
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import org.slf4j.{Logger, LoggerFactory}

object ScheduleRoutes {
  val log: Logger = LoggerFactory.getLogger(ScheduleRoutes.getClass)

  def routes[F[_]](repo: ScheduleRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._

    def loadScheduleForSeason(season: Season): F[Schedule] = {
      for {
        teams <- repo.listTeam()
        conferences <- repo.listConferences()
        conferenceMappings <- repo.findConferenceMappingBySeason(season.id)
        games <- repo.findGamesBySeason(season.id)
        results <- repo.findResultsBySeason(season.id)
      } yield {
        Schedule(season, teams, conferences, conferenceMappings, games, results)
      }
    }

    def routeScheduleRequest(value: F[Option[Season]]): F[Response[F]] = {
      (for {
        season <- value
        resp <- season match {
          case Some(seas) => Ok(loadScheduleForSeason(seas))
          case None => NotFound()
        }
      } yield {
        resp
      }).recoverWith { case thr: Throwable => InternalServerError(thr.getMessage) }
    }

    HttpRoutes.of[F] {
      case GET -> Root / "schedule" =>
        routeScheduleRequest(repo.findLatestSeason())
      case GET -> Root / "schedule" / IntVar(year) =>
        routeScheduleRequest(repo.findSeasonByYear(year))
    }
  }
}
