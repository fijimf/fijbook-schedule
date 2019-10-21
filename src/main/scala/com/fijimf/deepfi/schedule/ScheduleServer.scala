package com.fijimf.deepfi.schedule

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, Timer}
import cats.syntax.semigroupk._
import com.fijimf.deepfi.schedule.services.{ScheduleRepo, Snapshotter, Updater}
import doobie.util.transactor.Transactor
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.syntax.kleisli._
import org.http4s.{HttpApp, HttpRoutes}


object ScheduleServer {

  @SuppressWarnings(Array("org.wartremover.warts.Nothing", "org.wartremover.warts.Any"))
  def stream[F[_] : ConcurrentEffect](transactor: Transactor[F])(implicit T: Timer[F], C: ContextShift[F]): Stream[F, ExitCode] = {
    val repo = new ScheduleRepo[F](transactor)
    val healthcheckService: HttpRoutes[F] = ScheduleRoutes.healthcheckRoutes(repo)
    val aliasRepoService: HttpRoutes[F] = ScheduleRoutes.aliasRepoRoutes(repo)
    val repoService: HttpRoutes[F] = ScheduleRoutes.scheduleRepoRoutes(repo)
    val snapshotterService: HttpRoutes[F] = ScheduleRoutes.snapshotterRoutes(new Snapshotter[F](transactor))
    val updaterService: HttpRoutes[F] = ScheduleRoutes.updaterRoutes(new Updater[F](transactor))
    val httpApp: HttpApp[F] = (healthcheckService <+> aliasRepoService <+> repoService <+> snapshotterService <+> updaterService).orNotFound
    val finalHttpApp: HttpApp[F] = Logger.httpApp[F](logHeaders = true, logBody = true)(httpApp)
    for {
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(port = 8073, host = "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield {
      exitCode
    }
    }.drain


}
