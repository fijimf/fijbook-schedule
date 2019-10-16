package com.fijimf.deepfi.schedule
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfi.schedule.model._
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

  def scheduleRepoRoutes[F[_]](repo: ScheduleRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "alias" / LongVar(id) =>
        for {
          alias <- repo.findAlias(id)
          resp <- alias match {
            case Some(a) => Ok(a)
            case None => NotFound()
          }
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
      case GET -> Root / "season" / LongVar(id) =>
        for {
          season <- repo.findSeason(id)
          resp<-season match {
            case Some(s) => Ok(s)
            case None => NotFound()
          }
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
