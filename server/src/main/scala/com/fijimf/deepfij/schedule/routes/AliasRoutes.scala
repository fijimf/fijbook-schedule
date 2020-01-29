package com.fijimf.deepfij.schedule.routes

import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.model.{Alias, _}
import com.fijimf.deepfij.schedule.services.AliasRepo
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object AliasRoutes {
  def routes[F[_]](repo: AliasRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "alias" => //TODO potentially add lookup constraints
        (for {
          aliases <- repo.listAliases()
          resp <- Ok(aliases)
        } yield {
          resp
        }).recoverWith { case thr: Throwable => InternalServerError(thr.getMessage) }
      case GET -> Root / "alias" / LongVar(id) =>
        (for {
          alias <- repo.findAlias(id)
          resp <- alias match {
            case Some(a) => Ok(a)
            case None => NotFound()
          }
        } yield {
          resp
        }).recoverWith { case thr: Throwable => InternalServerError(thr.getMessage) }
      case req@POST -> Root / "alias" =>
        (for {
          a <- req.as[Alias]
          x <- a.id match {
            case 0 => repo.insertAlias(a)
            case _ => repo.updateAlias(a)
          }
          resp <- Ok(x)
        } yield {
          resp
        }).recoverWith { case thr: Throwable => InternalServerError(thr.getMessage) }

      case DELETE -> Root / "alias" / LongVar(id) =>
        (for {
          n <- repo.deleteAlias(id)
          resp <- Ok(n)
        } yield {
          resp
        }).recoverWith { case thr: Throwable => InternalServerError(thr.getMessage) }
    }
  }


}
