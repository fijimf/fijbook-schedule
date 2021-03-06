package com.fijimf.deepfij.schedule

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, Timer}
import cats.syntax.semigroupk._
import com.fijimf.deepfij.schedule.routes.{AliasRoutes, ConferenceMappingRoutes, ConferenceRoutes, GameRoutes, ResultRoutes, SeasonRoutes, TeamRoutes}
import com.fijimf.deepfij.schedule.services.{ScheduleRepo, Snapshotter, Updater}
import com.fijimf.deepfij.schedule.util.Banner
import doobie.util.transactor.Transactor
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.syntax.kleisli._
import org.http4s.{HttpApp, HttpRoutes}


object ScheduleServer {

  @SuppressWarnings(Array("org.wartremover.warts.Nothing", "org.wartremover.warts.Any"))
  def stream[F[_] : ConcurrentEffect](transactor: Transactor[F], port:Int)(implicit T: Timer[F], C: ContextShift[F]): Stream[F, ExitCode] = {
    val repo: ScheduleRepo[F] = new ScheduleRepo[F](transactor)
    val healthcheckService: HttpRoutes[F] = ScheduleRoutes.healthcheckRoutes(repo)
    val aliasRepoService: HttpRoutes[F] = AliasRoutes.routes(repo)
    val conferenceRepoService: HttpRoutes[F] = ConferenceRoutes.routes(repo)
    val conferenceMappingRepoService: HttpRoutes[F] = ConferenceMappingRoutes.routes(repo)
    val gameRepoService: HttpRoutes[F] = GameRoutes.routes(repo)
    val resultRepoService: HttpRoutes[F] = ResultRoutes.routes(repo)
    val seasonRepoService: HttpRoutes[F] = SeasonRoutes.routes(repo)
    val teamRepoService: HttpRoutes[F] = TeamRoutes.routes(repo)
    val scheduleService: HttpRoutes[F] = routes.ScheduleRoutes.routes(repo)
    val snapshotterService: HttpRoutes[F] = ScheduleRoutes.snapshotterRoutes(new Snapshotter[F](transactor))
    val updaterService: HttpRoutes[F] = ScheduleRoutes.updaterRoutes(new Updater[F](transactor))
    val httpApp: HttpApp[F] = (
      healthcheckService <+>
        aliasRepoService <+>
        conferenceRepoService <+>
        conferenceMappingRepoService <+>
        gameRepoService <+>
        resultRepoService <+>
        seasonRepoService <+>
        teamRepoService <+>
        scheduleService <+>
        snapshotterService <+>
        updaterService).orNotFound
    val finalHttpApp: HttpApp[F] = Logger.httpApp[F](logHeaders = true, logBody = true)(httpApp)
    val host = "0.0.0.0"
    for {
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(port = port, host = host)
        .withHttpApp(finalHttpApp)
        .withBanner(Banner.banner(host, port))
        .serve
    } yield {
      exitCode
    }
    }.drain


}
