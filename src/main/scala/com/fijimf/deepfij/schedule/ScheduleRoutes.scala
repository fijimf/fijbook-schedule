package com.fijimf.deepfij.schedule

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.services.{ ScheduleRepo, Snapshotter, Updater}
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
import org.slf4j.{Logger, LoggerFactory}

object ScheduleRoutes {
  val log: Logger = LoggerFactory.getLogger(ScheduleRoutes.getClass)

  implicit def intEntityEncoder[F[_] : Applicative]: EntityEncoder[F, Int] = jsonEncoderOf

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

  def updaterRoutes[F[_]](u: Updater[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@GET -> Root =>
        for {
          resp <- Ok()
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
          resp <- Ok()
        } yield {
          resp
        }
    }
  }
}
