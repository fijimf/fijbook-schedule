package com.fijimf.deepfi.schedule

import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfi.schedule.services.{ScheduleRepo, Snapshotter, Updater}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.slf4j.{Logger, LoggerFactory}

object ScheduleRoutes {
  val log: Logger = LoggerFactory.getLogger(ScheduleRoutes.getClass)


  def healthcheckRoutes[F[_]](r: ScheduleRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "healthcheck" =>
        for {
          resp <- Ok()
        } yield {
          resp
        }
    }
  }

  def scheduleRepoRoutes[F[_]](r: ScheduleRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root =>
        for {
          resp <- F.delay(Ok())
        } yield {
          resp
        }
    }
  }

  def updaterRoutes[F[_]](u: Updater[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root =>
        for {
          resp <- F.delay(Ok())
        } yield {
          resp
        }
    }
  }

  def snapshotterRoutes[F[_]](r: Snapshotter[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root =>
        for {
          resp <- F.delay(Ok())
        } yield {
          resp
        }
    }
  }
}
