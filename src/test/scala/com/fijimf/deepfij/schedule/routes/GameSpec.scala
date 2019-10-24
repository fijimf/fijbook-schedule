package com.fijimf.deepfij.schedule.routes

import java.sql.SQLException
import java.time.{LocalDate, LocalDateTime}

import cats.MonadError
import cats.data.{Kleisli, NonEmptyList}
import cats.effect.{Effect, IO}
import com.fijimf.deepfij.schedule.model.Game
import com.fijimf.deepfij.schedule.services.GameRepo
import org.http4s.implicits._
import org.http4s.{Method, Request, Response, Uri, _}
import org.scalatest.FunSpec

class GameSpec extends FunSpec {

  def service[F[_]] (repo: GameRepo[F])(implicit F: Effect[F]): Kleisli[F, Request[F], Response[F]] = GameRoutes.routes(repo).orNotFound

  val egGames: NonEmptyList[Game] = NonEmptyList.of(
    Game(1L,1L,LocalDate.of(2019,11,6),LocalDateTime.of(2019,11,6,19,0,0),12L,24L,None,Some(true), "20191106"),
    Game(2L,1L,LocalDate.of(2019,11,6),LocalDateTime.of(2019,11,6,19,0,0),33L,11L,None,Some(true), "20191106")
  )

  val happyPath: GameRepo[IO] = new GameRepo[IO] {

    val me = implicitly[MonadError[IO, Throwable]]
    override def insertGame(a: Game): IO[Game] = IO{a.copy(id=1L)}

    override def updateGame(a: Game): IO[Game] = IO{a}

    override def deleteGame(id: Long): IO[Int] = IO{1}

    override def listGame(): IO[List[Game]] = IO{ egGames.toList}

    override def findGame(id: Long): IO[Option[Game]] = IO{egGames.find(_.id===id)}
  }

  val happyButNotFound: GameRepo[IO] = new GameRepo[IO] {
     val me = implicitly[MonadError[IO, Throwable]]
    override def insertGame(a: Game): IO[Game] = IO{a.copy(id=1L)}

    override def updateGame(a: Game): IO[Game] = IO{a}

    override def deleteGame(id: Long): IO[Int] = IO{1}

    override def listGame(): IO[List[Game]] = IO{ List.empty[Game]}

    override def findGame(id: Long): IO[Option[Game]] = IO{None}
  }

  val sadSqlPath: GameRepo[IO] = new GameRepo[IO] {
    val me = implicitly[MonadError[IO, Throwable]]

    override def insertGame(a: Game): IO[Game] = me.raiseError(new SQLException("I get trapped"))

    override def updateGame(c: Game): IO[Game] = me.raiseError(new SQLException("I get trapped"))

    override def deleteGame(id: Long): IO[Int] = me.raiseError(new SQLException("I get trapped"))

    override def listGame(): IO[List[Game]] = me.raiseError(new SQLException("I get trapped"))

    override def findGame(id: Long): IO[Option[Game]] = me.raiseError(new SQLException("I get trapped"))
  }

  describe("GameRoutes should handle operations in the happy path ") {


    it ("find") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/game/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Game].unsafeRunSync()===egGames.head)
    }

    it ("list") {

      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/game"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[Game]].unsafeRunSync()===egGames.toList)
    }

    it ("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/game")).withEntity(egGames.head.copy(id=0L))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Game].unsafeRunSync()===egGames.head)
    }

    it ("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/game")).withEntity(egGames.head)
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Game].unsafeRunSync()===egGames.head)
    }

    it ("delete") {
      import com.fijimf.deepfij.schedule.model._
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/game/1"))
      val response: Response[IO] = service(happyPath).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[Int].unsafeRunSync()===1)
    }

  }
  describe("GameRoutes should handle operations when not found ") {
    it ("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/game/4"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.NotFound)
     assert(response.body.compile[IO,IO,Byte].toVector.unsafeRunSync().isEmpty)
    }

    it ("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/game"))
      val response: Response[IO] = service(happyButNotFound).run(request).unsafeRunSync()

      assert(response.status===Status.Ok)
      assert(response.as[List[Game]].unsafeRunSync()===List.empty[Game])
    }
  }
  describe("GameRoutes should handle operations in the sad path ") {
    it("find") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/game/4"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("list") {
      val request: Request[IO] = Request[IO](method = Method.GET, uri = Uri.uri("/game"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it ("insert") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/game")).withEntity(egGames.head.copy(id=0L))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status===Status.InternalServerError)
    }

    it("update") {
      val request: Request[IO] = Request[IO](method = Method.POST, uri = Uri.uri("/game")).withEntity(egGames.head)
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

    it("delete") {
      val request: Request[IO] = Request[IO](method = Method.DELETE, uri = Uri.uri("/game/1"))
      val response: Response[IO] = service(sadSqlPath).run(request).unsafeRunSync()
      assert(response.status === Status.InternalServerError)
    }

  }
}
