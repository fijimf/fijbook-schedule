package com.fijimf.deepfij.schedule.routes

import java.sql.SQLException

import cats.MonadError
import cats.data.{Kleisli, NonEmptyList}
import cats.effect.{Effect, IO}
import com.fijimf.deepfij.schedule.model.Team
import com.fijimf.deepfij.schedule.services.TeamRepo
import org.http4s.implicits._
import org.http4s.{Method, Request, Response, Uri, _}
import org.scalatest.FunSpec

class TeamSpec extends FunSpec {

  def service[F[_]](repo: TeamRepo[F])(implicit F: Effect[F]): Kleisli[F, Request[F], Response[F]] =
    TeamRoutes.routes(repo).orNotFound

  val egTeams: NonEmptyList[Team] = NonEmptyList.of(
    Team(1L, "georgetown", "Georgetown", "Hoyas", "", "blue", "gray"),
    Team(2L, "villanova", "Villanova", "Wildcats", "", "blue", "")
  )
  val happyPath: TeamRepo[IO] = new TeamRepo[IO] {
    val me = implicitly[MonadError[IO, Throwable]]

    override def insertTeam(t: Team): IO[Team] = IO {
      t.copy(id = 99L)
    }

    override def updateTeam(t: Team): IO[Team] = IO {
      t
    }

    override def deleteTeam(id: Long): IO[Int] = IO {
      1
    }

    override def listTeam(): IO[List[Team]] = IO {
      egTeams.toList
    }

    override def findTeam(id: Long): IO[Option[Team]] = IO {
      Some(egTeams.head)
    }
  }

  val happyButNotFound: TeamRepo[IO] = new TeamRepo[IO] {
    val me = implicitly[MonadError[IO, Throwable]]

    override def insertTeam(a: Team): IO[Team] = IO {
      a.copy(id = 1L)
    }

    override def updateTeam(a: Team): IO[Team] = IO {
      a
    }

    override def deleteTeam(id: Long): IO[Int] = IO {
      1
    }

    override def listTeam(): IO[List[Team]] = IO {
      List.empty[Team]
    }

    override def findTeam(id: Long): IO[Option[Team]] = IO {
      None
    }
  }

  val sadSqlPath: TeamRepo[IO] = new TeamRepo[IO] {
    val me = implicitly[MonadError[IO, Throwable]]

    override def insertTeam(a: Team): IO[Team] = me.raiseError(new SQLException("I get trapped"))

    override def updateTeam(c: Team): IO[Team] = me.raiseError(new SQLException("I get trapped"))

    override def deleteTeam(id: Long): IO[Int] = me.raiseError(new SQLException("I get trapped"))

    override def listTeam(): IO[List[Team]] = me.raiseError(new SQLException("I get trapped"))

    override def findTeam(id: Long): IO[Option[Team]] = me.raiseError(new SQLException("I get trapped"))
  }

  describe("TeamRoutes should handle operations in the happy path ") {
    it("find") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/team/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[Team].unsafeRunSync() === egTeams.head)
    }

    it("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/team"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[List[Team]].unsafeRunSync() === egTeams.toList)
    }

    it("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/team")).withEntity(egTeams.head.copy(id = 0))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[Team].unsafeRunSync() === egTeams.head.copy(id=99))
    }

    it("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/team")).withEntity(egTeams.head)
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[Team].unsafeRunSync() === egTeams.head)
    }

    it("delete") {
      import com.fijimf.deepfij.schedule.model._
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/team/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[Int].unsafeRunSync() === 1)
    }

  }
  describe("TeamRoutes should handle operations when not found ") {
    it("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/team/4"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status === Status.NotFound)
      assert(response.body.compile[IO, IO, Byte].toVector.unsafeRunSync().isEmpty)
    }

    it("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/team"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status === Status.Ok)
      assert(response.as[List[Team]].unsafeRunSync() === List.empty[Team])
    }
  }
  describe("TeamRoutes should handle operations in the sad path ") {
    it("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/team/4"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/team"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/team")).withEntity(egTeams.head.copy(id = 0))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/team")).withEntity(egTeams.head)
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("delete") {
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/team/1"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

  }
}
