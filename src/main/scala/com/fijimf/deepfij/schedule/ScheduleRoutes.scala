package com.fijimf.deepfij.schedule

import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import com.fijimf.deepfij.schedule.model._
import com.fijimf.deepfij.schedule.services.{AliasRepo, ScheduleRepo, Snapshotter, Updater}
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

  def aliasRepoRoutes[F[_]](repo: AliasRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
    val dsl: Http4sDsl[F] = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "alias" => //TODO potentially add lookup constraints
        for {
          aliases <- repo.listAliases()
          resp <- Ok(aliases)
        } yield {
          resp
        }
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
      case req@POST -> Root / "alias" =>
        for {
          a <- req.as[Alias]
          x <- a.id match {
            case 0 => repo.insertAlias(a)
            case _ => repo.updateAlias(a)
          }
          resp <- Ok(x)
        } yield {
          resp
        }

      case DELETE -> Root / "alias" / LongVar(id) =>
        for {
          n <- repo.deleteAlias(id)
          resp <- Ok(n)
        } yield {
          resp
        }
    }
  }

  def scheduleRepoRoutes[F[_]](repo: ScheduleRepo[F])(implicit F: Sync[F]): HttpRoutes[F] = {
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
          resp<-season match {
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
