package com.fijimf.deepfij.schedule.routes

import java.sql.SQLException

import cats.MonadError
import cats.data.{Kleisli, NonEmptyList}
import cats.effect.{Effect, IO}
import com.fijimf.deepfij.schedule.model.ConferenceMapping
import com.fijimf.deepfij.schedule.services.ConferenceMappingRepo
import org.http4s.implicits._
import org.http4s.{Method, Request, Response, Uri, _}
import org.scalatest.FunSpec

class ConferenceMappingSpec extends FunSpec {

  def service[F[_]] (repo: ConferenceMappingRepo[F])(implicit F: Effect[F]): Kleisli[F, Request[F], Response[F]] = ConferenceMappingRoutes.routes(repo).orNotFound

  val egConferenceMappings: NonEmptyList[ConferenceMapping] = NonEmptyList.of(
    ConferenceMapping(1L,1L,11L, 3L),
    ConferenceMapping(2L,1L,10L, 3L)
  )

  val happyPath: ConferenceMappingRepo[IO] = new ConferenceMappingRepo[IO] {

    val me = implicitly[MonadError[IO, Throwable]]
    override def insertConferenceMapping(a: ConferenceMapping): IO[ConferenceMapping] = IO{a.copy(id=1L)}

    override def updateConferenceMapping(a: ConferenceMapping): IO[ConferenceMapping] = IO{a}

    override def deleteConferenceMapping(id: Long): IO[Int] = IO{1}

    override def listConferenceMappings(): IO[List[ConferenceMapping]] = IO{ egConferenceMappings.toList}

    override def findConferenceMapping(id: Long): IO[Option[ConferenceMapping]] = IO{egConferenceMappings.find(_.id===id)}
  }

  val happyButNotFound: ConferenceMappingRepo[IO] = new ConferenceMappingRepo[IO] {
     val me = implicitly[MonadError[IO, Throwable]]
    override def insertConferenceMapping(a: ConferenceMapping): IO[ConferenceMapping] = IO{a.copy(id=1L)}

    override def updateConferenceMapping(a: ConferenceMapping): IO[ConferenceMapping] = IO{a}

    override def deleteConferenceMapping(id: Long): IO[Int] = IO{1}

    override def listConferenceMappings(): IO[List[ConferenceMapping]] = IO{ List.empty[ConferenceMapping]}

    override def findConferenceMapping(id: Long): IO[Option[ConferenceMapping]] = IO{None}
  }

  val sadSqlPath: ConferenceMappingRepo[IO] = new ConferenceMappingRepo[IO] {
    val me = implicitly[MonadError[IO, Throwable]]

    override def insertConferenceMapping(a: ConferenceMapping): IO[ConferenceMapping] = me.raiseError(new SQLException("I get trapped"))

    override def updateConferenceMapping(c: ConferenceMapping): IO[ConferenceMapping] = me.raiseError(new SQLException("I get trapped"))

    override def deleteConferenceMapping(id: Long): IO[Int] = me.raiseError(new SQLException("I get trapped"))

    override def listConferenceMappings(): IO[List[ConferenceMapping]] = me.raiseError(new SQLException("I get trapped"))

    override def findConferenceMapping(id: Long): IO[Option[ConferenceMapping]] = me.raiseError(new SQLException("I get trapped"))
  }

  describe("ConferenceMappingRoutes should handle operations in the happy path ") {


    it ("find") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conferenceMapping/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[ConferenceMapping].unsafeRunSync()===egConferenceMappings.head)
    }

    it ("list") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conferenceMapping"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[ConferenceMapping]].unsafeRunSync()===egConferenceMappings.toList)
    }

    it ("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/conferenceMapping")).withEntity(egConferenceMappings.head.copy(id=0L))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[ConferenceMapping].unsafeRunSync()===egConferenceMappings.head)
    }

    it ("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/conferenceMapping")).withEntity(egConferenceMappings.head)
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[ConferenceMapping].unsafeRunSync()===egConferenceMappings.head)
    }

    it ("delete") {
      import com.fijimf.deepfij.schedule.model._
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/conferenceMapping/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Int].unsafeRunSync()===1)
    }

  }
  describe("ConferenceMappingRoutes should handle operations when not found ") {
    it ("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conferenceMapping/4"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.NotFound)
     assert(response.body.compile[IO,IO,Byte].toVector.unsafeRunSync().isEmpty)
    }

    it ("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conferenceMapping"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[ConferenceMapping]].unsafeRunSync()===List.empty[ConferenceMapping])
    }
  }
  describe("ConferenceMappingRoutes should handle operations in the sad path ") {
    it("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conferenceMapping/4"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/conferenceMapping"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it ("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/conferenceMapping")).withEntity(egConferenceMappings.head.copy(id=0L))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status===Status.InternalServerError)
    }

    it("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/conferenceMapping")).withEntity(egConferenceMappings.head)
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("delete") {
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/conferenceMapping/1"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

  }
}
