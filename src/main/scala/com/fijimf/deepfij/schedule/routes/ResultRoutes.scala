package com.fijimf.deepfij.schedule.routes

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.model._
import com.fijimf.deepfij.schedule.services.{ ResultRepo}
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}
import org.slf4j.{Logger, LoggerFactory}

object ResultRoutes {
  val log: Logger = LoggerFactory.getLogger(ResultRoutes.getClass)

  def routes[F[_]](repo: ResultRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {

      case GET -> Root / "result" =>
        for {
          results <- repo.listResult()
          resp <- Ok(results)
        } yield {
          resp
        }
      case GET -> Root / "result" / LongVar(id) =>
        for {
          result <- repo.findResult(id)
          resp <- result match {
            case Some(r) => Ok(r)
            case None => NotFound()
          }

        } yield {
          resp
        }
      case req@POST -> Root / "result" =>
        for {
          r <- req.as[Result]
          x <- r.id match {
            case 0 => repo.insertResult(r)
            case _ => repo.updateResult(r)
          }
          resp <- Ok(x)
        } yield {
          resp
        }
      case DELETE -> Root / "result" / LongVar(id) =>
        for {
          n <- repo.deleteResult(id)
          resp <- Ok(n)
        } yield {
          resp
        }

    }
  }
}
