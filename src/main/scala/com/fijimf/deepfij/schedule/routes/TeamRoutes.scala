package com.fijimf.deepfij.schedule.routes

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.model._
import com.fijimf.deepfij.schedule.services.{TeamRepo}
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
import org.slf4j.{Logger, LoggerFactory}

object TeamRoutes {
  val log: Logger = LoggerFactory.getLogger(TeamRoutes.getClass)


  def routes[F[_]](repo: TeamRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {

      case GET -> Root / "team" =>
        for {
          teams <- repo.listTeam()
          resp <- Ok(teams)
        } yield {
          resp
        }
      case GET -> Root / "team" / LongVar(id) =>
        for {
          team <- repo.findTeam(id)
          resp <- team match {
            case Some(t) => Ok(t)
            case None => NotFound()
          }
        } yield {
          resp
        }
      case req@POST -> Root / "team" =>
        for {
          t <- req.as[Team]
          x <- t.id match {
            case 0 => repo.insertTeam(t)
            case _ => repo.updateTeam(t)
          }
          resp <- Ok(x)
        } yield {
          resp
        }
      case DELETE -> Root / "team" / LongVar(id) =>
        for {
          n <- repo.deleteTeam(id)
          resp <- Ok(n)
        } yield {
          resp
        }

    }
  }
}
