package com.fijimf.deepfij.schedule.routes

import java.sql.SQLException

import cats.MonadError
import cats.data.{Kleisli, NonEmptyList}
import cats.effect.{Effect, IO}
import com.fijimf.deepfij.schedule.model.Result
import com.fijimf.deepfij.schedule.services.ResultRepo
import org.http4s.implicits._
import org.http4s.{Method, Request, Response, Uri, _}
import org.scalatest.FunSpec

class ResultSpec extends FunSpec {

  def service[F[_]] (repo: ResultRepo[F])(implicit F: Effect[F]): Kleisli[F, Request[F], Response[F]] = ResultRoutes.routes(repo).orNotFound

  val egResults: NonEmptyList[Result] = NonEmptyList.of(
    Result(1L,100L, 100,99, 2),
    Result(2L,101L, 88, 63, 2)
  )

  val happyPath: ResultRepo[IO] = new ResultRepo[IO] {

    val me = implicitly[MonadError[IO, Throwable]]
    override def insertResult(a: Result): IO[Result] = IO{a.copy(id=1L)}

    override def updateResult(a: Result): IO[Result] = IO{a}

    override def deleteResult(id: Long): IO[Int] = IO{1}

    override def listResult(): IO[List[Result]] = IO{ egResults.toList}

    override def findResult(id: Long): IO[Option[Result]] = IO{egResults.find(_.id===id)}

    override def findResultsBySeason(id: Long): IO[List[Result]] = IO{egResults.toList}
  }

  val happyButNotFound: ResultRepo[IO] = new ResultRepo[IO] {
     val me = implicitly[MonadError[IO, Throwable]]
    override def insertResult(a: Result): IO[Result] = IO{a.copy(id=1L)}

    override def updateResult(a: Result): IO[Result] = IO{a}

    override def deleteResult(id: Long): IO[Int] = IO{1}

    override def listResult(): IO[List[Result]] = IO{ List.empty[Result]}

    override def findResult(id: Long): IO[Option[Result]] = IO{None}

    override def findResultsBySeason(id: Long): IO[List[Result]] = IO{List.empty[Result]}
  }

  val sadSqlPath: ResultRepo[IO] = new ResultRepo[IO] {
    val me = implicitly[MonadError[IO, Throwable]]

    override def insertResult(a: Result): IO[Result] = me.raiseError(new SQLException("I get trapped"))

    override def updateResult(c: Result): IO[Result] = me.raiseError(new SQLException("I get trapped"))

    override def deleteResult(id: Long): IO[Int] = me.raiseError(new SQLException("I get trapped"))

    override def listResult(): IO[List[Result]] = me.raiseError(new SQLException("I get trapped"))

    override def findResult(id: Long): IO[Option[Result]] = me.raiseError(new SQLException("I get trapped"))

    override def findResultsBySeason(id: Long): IO[List[Result]] = me.raiseError(new SQLException("I get trapped"))
  }

  describe("ResultRoutes should handle operations in the happy path ") {


    it ("find") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/result/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Result].unsafeRunSync()===egResults.head)
    }

    it ("list") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/result"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[Result]].unsafeRunSync()===egResults.toList)
    }

    it ("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/result")).withEntity(egResults.head.copy(id=0L))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Result].unsafeRunSync()===egResults.head)
    }

    it ("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/result")).withEntity(egResults.head)
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Result].unsafeRunSync()===egResults.head)
    }

    it ("delete") {
      import com.fijimf.deepfij.schedule.model._
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/result/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Int].unsafeRunSync()===1)
    }

  }
  describe("ResultRoutes should handle operations when not found ") {
    it ("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/result/4"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.NotFound)
     assert(response.body.compile[IO,IO,Byte].toVector.unsafeRunSync().isEmpty)
    }

    it ("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/result"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[Result]].unsafeRunSync()===List.empty[Result])
    }
  }
  describe("ResultRoutes should handle operations in the sad path ") {
    it("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/result/4"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/result"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it ("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/result")).withEntity(egResults.head.copy(id=0L))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status===Status.InternalServerError)
    }

    it("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/result")).withEntity(egResults.head)
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("delete") {
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/result/1"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

  }
}
