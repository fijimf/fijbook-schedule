package com.fijimf.deepfij.schedule.routes

import cats.data.{Kleisli, OptionT}
import cats.{Id, effect}
import cats.effect.{Effect, IO}
import com.fijimf.deepfi.schedule.ScheduleRoutes
import com.fijimf.deepfi.schedule.model.Alias
import com.fijimf.deepfi.schedule.services.AliasRepo
import org.http4s.{HttpRoutes, HttpService, Method, Request, Response, Uri}
import org.http4s.implicits._
import org.http4s._
import org.scalatest.FunSpec
import com.fijimf.deepfij.schedule._

class AliasSpec extends FunSpec {

  def service[F[_]] (repo: AliasRepo[F])(implicit F: Effect[F]): Kleisli[F, Request[F], Response[F]] = ScheduleRoutes.aliasRepoRoutes(repo).orNotFound
  val happyPath: AliasRepo[IO] = new AliasRepo[IO] {
    override def insertAlias(a: Alias): IO[Alias] = IO{a.copy(id=1L)}

    override def updateAlias(a: Alias): IO[Alias] = IO{a}

    override def deleteAlias(id: Long): IO[Int] = IO{1}

    override def listAliases(): IO[List[Alias]] = IO{ List( Alias(1L,23L,"usc"),Alias(1L,29L,"san-diego"))}

    override def findAlias(id: Long): IO[Option[Alias]] = IO{Some(Alias(1L,23L,"usc"))}
  }
  describe("AliasRoutes should handle operations in the happy path ") {

    it ("should handle insert") {
      val alias=Alias(0L,23L,"usc")
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/alias")).withEntity(alias)
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Alias].unsafeRunSync()===alias.copy(id=1))
    }

    it ("should handle update") {
      val alias=Alias(3L,23L,"usc")
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/alias")).withEntity(alias)
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Alias].unsafeRunSync()===alias)
    }

  }
}
