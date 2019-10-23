package com.fijimf.deepfij.schedule.routes

import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.model._
import com.fijimf.deepfij.schedule.services.{ConferenceRepo}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.slf4j.{Logger, LoggerFactory}

object ConferenceRoutes {
  val log: Logger = LoggerFactory.getLogger(ConferenceRoutes.getClass)


  def routes[F[_]](repo: ConferenceRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "conference" => //TODO potentially add lookup constraints
        for {
          conferences <- repo.listConferences()
          resp <- Ok(conferences)
        } yield {
          resp
        }
      case GET -> Root / "conference" / LongVar(id) =>
        for {
          conference <- repo.findConference(id)
          resp <- conference match {
            case Some(c) => Ok(c)
            case None => NotFound()
          }
        } yield {
          resp
        }
      case req@POST -> Root / "conference" =>
        for {
          c <- req.as[Conference]
          x <- c.id match {
            case 0 => repo.insertConference(c)
            case _ => repo.updateConference(c)
          }
          resp <- Ok(x)
        } yield {
          resp
        }
      case DELETE -> Root / "conference" / LongVar(id) =>
        for {
          n <- repo.deleteConference(id)
          resp <- Ok(n)
        } yield {
          resp
        }
    }
  }
}
