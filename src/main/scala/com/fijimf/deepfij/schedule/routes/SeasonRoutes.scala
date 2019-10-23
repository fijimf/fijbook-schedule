package com.fijimf.deepfij.schedule.routes

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.model._
import com.fijimf.deepfij.schedule.services.{SeasonRepo}
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
import org.slf4j.{Logger, LoggerFactory}

object SeasonRoutes {
  val log: Logger = LoggerFactory.getLogger(SeasonRoutes.getClass)

  def routes[F[_]](repo: SeasonRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {

      case GET -> Root / "season" =>
        for {
          seasons <- repo.listSeason()
          resp <- Ok(seasons)
        } yield {
          resp
        }
      case GET -> Root / "season" / LongVar(id) =>
        for {
          season <- repo.findSeason(id)
          resp <- season match {
            case Some(s) => Ok(s)
            case None => NotFound()
          }
        } yield {
          resp
        }
      case req@POST -> Root / "season" =>
        for {
          s <- req.as[Season]
          x <- s.id match {
            case 0 => repo.insertSeason(s)
            case _ => repo.updateSeason(s)
          }
          resp <- Ok(x)
        } yield {
          resp
        }
      case DELETE -> Root / "season" / LongVar(id) =>
        for {
          n <- repo.deleteSeason(id)
          resp <- Ok(n)
        } yield {
          resp
        }
    }
  }
}
