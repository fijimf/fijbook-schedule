package com.fijimf.deepfij.schedule.routes

import java.sql.SQLException

import cats.MonadError
import cats.data.{Kleisli, NonEmptyList}
import cats.effect.{Effect, IO}
import com.fijimf.deepfij.schedule.model.Season
import com.fijimf.deepfij.schedule.services.SeasonRepo
import org.http4s.implicits._
import org.http4s.{Method, Request, Response, Uri, _}
import org.scalatest.FunSpec

class SeasonSpec extends FunSpec {

  def service[F[_]] (repo: SeasonRepo[F])(implicit F: Effect[F]): Kleisli[F, Request[F], Response[F]] = SeasonRoutes.routes(repo).orNotFound

  val egSeasons: NonEmptyList[Season] = NonEmptyList.of(
    Season(1L,2019),
    Season(2L,2020)
  )

  val happyPath: SeasonRepo[IO] = new SeasonRepo[IO] {

    val me = implicitly[MonadError[IO, Throwable]]
    override def insertSeason(a: Season): IO[Season] = IO{a.copy(id=1L)}

    override def updateSeason(a: Season): IO[Season] = IO{a}

    override def deleteSeason(id: Long): IO[Int] = IO{1}

    override def listSeason(): IO[List[Season]] = IO{ egSeasons.toList}

    override def findSeason(id: Long): IO[Option[Season]] = IO{egSeasons.find(_.id===id)}
  }

  val happyButNotFound: SeasonRepo[IO] = new SeasonRepo[IO] {
     val me = implicitly[MonadError[IO, Throwable]]
    override def insertSeason(a: Season): IO[Season] = IO{a.copy(id=1L)}

    override def updateSeason(a: Season): IO[Season] = IO{a}

    override def deleteSeason(id: Long): IO[Int] = IO{1}

    override def listSeason(): IO[List[Season]] = IO{ List.empty[Season]}

    override def findSeason(id: Long): IO[Option[Season]] = IO{None}
  }

  val sadSqlPath: SeasonRepo[IO] = new SeasonRepo[IO] {
    val me = implicitly[MonadError[IO, Throwable]]

    override def insertSeason(a: Season): IO[Season] = me.raiseError(new SQLException("I get trapped"))

    override def updateSeason(c: Season): IO[Season] = me.raiseError(new SQLException("I get trapped"))

    override def deleteSeason(id: Long): IO[Int] = me.raiseError(new SQLException("I get trapped"))

    override def listSeason(): IO[List[Season]] = me.raiseError(new SQLException("I get trapped"))

    override def findSeason(id: Long): IO[Option[Season]] = me.raiseError(new SQLException("I get trapped"))
  }

  describe("SeasonRoutes should handle operations in the happy path ") {


    it ("find") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/season/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Season].unsafeRunSync()===egSeasons.head)
    }

    it ("list") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/season"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[Season]].unsafeRunSync()===egSeasons.toList)
    }

    it ("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/season")).withEntity(egSeasons.head.copy(id=0L))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Season].unsafeRunSync()===egSeasons.head)
    }

    it ("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/season")).withEntity(egSeasons.head)
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Season].unsafeRunSync()===egSeasons.head)
    }

    it ("delete") {
      import com.fijimf.deepfij.schedule.model._
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/season/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Int].unsafeRunSync()===1)
    }

  }
  describe("SeasonRoutes should handle operations when not found ") {
    it ("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/season/4"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.NotFound)
     assert(response.body.compile[IO,IO,Byte].toVector.unsafeRunSync().isEmpty)
    }

    it ("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/season"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[Season]].unsafeRunSync()===List.empty[Season])
    }
  }
  describe("SeasonRoutes should handle operations in the sad path ") {
    it("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/season/4"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/season"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it ("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/season")).withEntity(egSeasons.head.copy(id=0L))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status===Status.InternalServerError)
    }

    it("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/season")).withEntity(egSeasons.head)
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("delete") {
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/season/1"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

  }
}
