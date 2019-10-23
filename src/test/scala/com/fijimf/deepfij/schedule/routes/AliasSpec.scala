package com.fijimf.deepfij.schedule.routes

import java.sql.SQLException

import cats.MonadError
import cats.data.Kleisli
import cats.effect.{Effect, IO}
import com.fijimf.deepfij.schedule.model.Alias
import com.fijimf.deepfij.schedule.services.AliasRepo
import org.http4s.{Method, Request, Response, Uri}
import org.http4s.implicits._
import org.http4s._
import org.scalatest.FunSpec

class AliasSpec extends FunSpec {

  def service[F[_]] (repo: AliasRepo[F])(implicit F: Effect[F]): Kleisli[F, Request[F], Response[F]] = AliasRoutes.routes(repo).orNotFound

  val happyPath: AliasRepo[IO] = new AliasRepo[IO] {
    override def insertAlias(a: Alias)(implicit me: MonadError[IO, Throwable]): IO[Alias] = IO{a.copy(id=1L)}

    override def updateAlias(a: Alias): IO[Alias] = IO{a}

    override def deleteAlias(id: Long): IO[Int] = IO{1}

    override def listAliases(): IO[List[Alias]] = IO{ List( Alias(1L,23L,"usc"),Alias(1L,29L,"san-diego"))}

    override def findAlias(id: Long): IO[Option[Alias]] = IO{Some(Alias(4L,23L,"usc"))}
  }

  val happyButNotFound: AliasRepo[IO] = new AliasRepo[IO] {
    override def insertAlias(a: Alias)(implicit me: MonadError[IO, Throwable]): IO[Alias] = IO{a.copy(id=1L)}

    override def updateAlias(a: Alias): IO[Alias] = IO{a}

    override def deleteAlias(id: Long): IO[Int] = IO{1}

    override def listAliases(): IO[List[Alias]] = IO{ List.empty[Alias]}

    override def findAlias(id: Long): IO[Option[Alias]] = IO{None}
  }

  val sadSqlPath: AliasRepo[IO] = new AliasRepo[IO] {
    override def insertAlias(a: Alias)(implicit me: MonadError[IO, Throwable]): IO[Alias] = me.raiseError(new SQLException("test"))

    override def updateAlias(c: Alias): IO[Alias] = throw new RuntimeException

    override def deleteAlias(id: Long): IO[Int] = throw new RuntimeException

    override def listAliases(): IO[List[Alias]] = throw new RuntimeException

    override def findAlias(id: Long): IO[Option[Alias]] = throw new RuntimeException
  }

  describe("AliasRoutes should handle operations in the happy path ") {
    it ("find") {
      val alias=Alias(4L,23L,"usc")
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/alias/4"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Alias].unsafeRunSync()===alias)
    }

    it ("list") {
      val alias=Alias(0L,23L,"usc")
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/alias"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[Alias]].unsafeRunSync()===List( Alias(1L,23L,"usc"),Alias(1L,29L,"san-diego")))
    }

    it ("insert") {
      val alias=Alias(0L,23L,"usc")
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/alias")).withEntity(alias)
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Alias].unsafeRunSync()===alias.copy(id=1))
    }

    it ("update") {
      val alias=Alias(3L,23L,"usc")
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/alias")).withEntity(alias)
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Alias].unsafeRunSync()===alias)
    }

    it ("delete") {
      import com.fijimf.deepfij.schedule.model._
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/alias/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Int].unsafeRunSync()===1)
    }

  }
  describe("AliasRoutes should handle operations when not found ") {
    it ("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/alias/4"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.NotFound)
     assert(response.body.compile[IO,IO,Byte].toVector.unsafeRunSync().isEmpty)
    }

    it ("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/alias"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[Alias]].unsafeRunSync()===List.empty[Alias])
    }
  }
  describe("AliasRoutes should handle operations in the sad path ") {
//    it ("find") {
//
//      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/alias/4"))
//      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
//
//      assert(response.status===Status.Ok)
//
//    }

//    it ("list") {
//      val alias=Alias(0L,23L,"usc")
//      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/alias"))
//      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()
//
//      assert(response.status===Status.Ok)
//      assert(response.as[List[Alias]].unsafeRunSync()===List( Alias(1L,23L,"usc"),Alias(1L,29L,"san-diego")))
//    }

    it ("insert") {
      val alias=Alias(0L,23L,"usc")
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/alias")).withEntity(alias)
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()

      assert(response.status===Status.InternalServerError)
    }

//    it ("update") {
//      val alias=Alias(3L,23L,"usc")
//      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/alias")).withEntity(alias)
//      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()
//
//      assert(response.status===Status.Ok)
//      assert(response.as[Alias].unsafeRunSync()===alias)
//    }
//
//    it ("delete") {
//      import com.fijimf.deepfij.schedule.model._
//      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/alias/1"))
//      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()
//
//      assert(response.status===Status.Ok)
//      assert(response.as[Int].unsafeRunSync()===1)
//    }

  }
}
