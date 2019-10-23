package com.fijimf.deepfij.schedule.routes

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.model._
import com.fijimf.deepfij.schedule.services.{GameRepo}
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
import org.slf4j.{Logger, LoggerFactory}

object GameRoutes {
  val log: Logger = LoggerFactory.getLogger(GameRoutes.getClass)

  def routes[F[_]](repo: GameRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {


      case GET -> Root / "game" =>
        for {
          games <- repo.listGame()
          resp <- Ok(games)
        } yield {
          resp
        }
      case GET -> Root / "game" / LongVar(id) =>
        for {
          game <- repo.findGame(id)
          resp <- game match {
            case Some(g) => Ok(g)
            case None => NotFound()
          }
        } yield {
          resp
        }
      case req@POST -> Root / "game" =>
        for {
          g <- req.as[Game]
          x <- g.id match {
            case 0 => repo.insertGame(g)
            case _ => repo.updateGame(g)
          }
          resp <- Ok(x)
        } yield {
          resp
        }
      case DELETE -> Root / "game" / LongVar(id) =>
        for {
          n <- repo.deleteGame(id)
          resp <- Ok(n)
        } yield {
          resp
        }


    }
  }
}
