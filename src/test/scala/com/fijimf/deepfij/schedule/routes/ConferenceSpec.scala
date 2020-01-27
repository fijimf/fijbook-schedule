package com.fijimf.deepfij.schedule.routes

import java.sql.SQLException

import cats.MonadError
import cats.data.{Kleisli, NonEmptyList}
import cats.effect.{Effect, IO}
import com.fijimf.deepfij.schedule.model.Conference
import com.fijimf.deepfij.schedule.services.ConferenceRepo
import org.http4s.implicits._
import org.http4s.{Method, Request, Response, Uri, _}
import org.scalatest.FunSpec

class ConferenceSpec extends FunSpec {

  def service[F[_]] (repo: ConferenceRepo[F])(implicit F: Effect[F]): Kleisli[F, Request[F], Response[F]] = ConferenceRoutes.routes(repo).orNotFound

  val egConferences: NonEmptyList[Conference] = NonEmptyList.of(
    Conference(1L,"big-east","Big East", "The Big East Conference","High Major", Some("xxx")),
    Conference(2L,"big-12","Big XII", "The Big XII Conference","Mid-Major", Some("xxx"))
  )

  val happyPath: ConferenceRepo[IO] = new ConferenceRepo[IO] {

    val me = implicitly[MonadError[IO, Throwable]]
    override def insertConference(a: Conference): IO[Conference] = IO{a.copy(id=1L)}

    override def updateConference(a: Conference): IO[Conference] = IO{a}

    override def deleteConference(id: Long): IO[Int] = IO{1}

    override def listConferences(): IO[List[Conference]] = IO{ egConferences.toList}

    override def findConference(id: Long): IO[Option[Conference]] = IO{egConferences.find(_.id===id)}
  }

  val happyButNotFound: ConferenceRepo[IO] = new ConferenceRepo[IO] {
     val me = implicitly[MonadError[IO, Throwable]]
    override def insertConference(a: Conference): IO[Conference] = IO{a.copy(id=1L)}

    override def updateConference(a: Conference): IO[Conference] = IO{a}

    override def deleteConference(id: Long): IO[Int] = IO{1}

    override def listConferences(): IO[List[Conference]] = IO{ List.empty[Conference]}

    override def findConference(id: Long): IO[Option[Conference]] = IO{None}
  }

  val sadSqlPath: ConferenceRepo[IO] = new ConferenceRepo[IO] {
    val me = implicitly[MonadError[IO, Throwable]]

    override def insertConference(a: Conference): IO[Conference] = me.raiseError(new SQLException("I get trapped"))

    override def updateConference(c: Conference): IO[Conference] = me.raiseError(new SQLException("I get trapped"))

    override def deleteConference(id: Long): IO[Int] = me.raiseError(new SQLException("I get trapped"))

    override def listConferences(): IO[List[Conference]] = me.raiseError(new SQLException("I get trapped"))

    override def findConference(id: Long): IO[Option[Conference]] = me.raiseError(new SQLException("I get trapped"))
  }

  describe("ConferenceRoutes should handle operations in the happy path ") {


    it ("find") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conference/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Conference].unsafeRunSync()===egConferences.head)
    }

    it ("list") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conference"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[Conference]].unsafeRunSync()===egConferences.toList)
    }

    it ("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/conference")).withEntity(egConferences.head.copy(id=0L))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Conference].unsafeRunSync()===egConferences.head)
    }

    it ("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/conference")).withEntity(egConferences.head)
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Conference].unsafeRunSync()===egConferences.head)
    }

    it ("delete") {
      import com.fijimf.deepfij.schedule.model._
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/conference/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Int].unsafeRunSync()===1)
    }

  }
  describe("ConferenceRoutes should handle operations when not found ") {
    it ("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conference/4"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.NotFound)
     assert(response.body.compile[IO,IO,Byte].toVector.unsafeRunSync().isEmpty)
    }

    it ("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conference"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[Conference]].unsafeRunSync()===List.empty[Conference])
    }
  }
  describe("ConferenceRoutes should handle operations in the sad path ") {
    it("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conference/4"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conference"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it ("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/conference")).withEntity(egConferences.head.copy(id=0L))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status===Status.InternalServerError)
    }

    it("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/conference")).withEntity(egConferences.head)
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("delete") {
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/conference/1"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

  }
}
