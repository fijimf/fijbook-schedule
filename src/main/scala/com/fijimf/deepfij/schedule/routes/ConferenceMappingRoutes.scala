package com.fijimf.deepfij.schedule.routes

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.model._
import com.fijimf.deepfij.schedule.services.{ ConferenceMappingRepo}
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
import org.slf4j.{Logger, LoggerFactory}

object ConferenceMappingRoutes {
  val log: Logger = LoggerFactory.getLogger(ConferenceMappingRoutes.getClass)


  def routes[F[_]](repo: ConferenceMappingRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {

      case GET -> Root / "conferenceMapping" =>
        for {
          cms <- repo.listConferenceMappings()
          resp <- Ok(cms)
        } yield {
          resp
        }
      case GET -> Root / "conferenceMapping" / LongVar(id) =>
        for {
          conferenceMapping <- repo.findConferenceMapping(id)
          resp <- conferenceMapping match {
            case Some(cm) => Ok(cm)
            case None => NotFound()
          }
        } yield {
          resp
        }
      case req@POST -> Root / "conferenceMapping" =>
        for {
          cm <- req.as[ConferenceMapping]
          x <- cm.id match {
            case 0 => repo.insertConferenceMapping(cm)
            case _ => repo.updateConferenceMapping(cm)
          }
          resp <- Ok(x)
        } yield {
          resp
        }
      case DELETE -> Root / "conferenceMapping" / LongVar(id) =>
        for {
          n <- repo.deleteConferenceMapping(id)
          resp <- Ok(n)
        } yield {
          resp
        }

    }
  }
}
