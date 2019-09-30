package com.fijimf.deepfij.schedule

import java.time.{LocalDate, LocalDateTime}

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfi.schedule.model.{Game, Result, Team}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.{Colors, ExecutionContexts}
import org.scalatest.{FunSuite, Matchers}


class ResultSpec extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
  )

  test("insert") {
    check(Result.Dao.insert(Result(0L, 1L, 92,67,2)))
  }


  test("list") {
    check(Result.Dao.list())
  }

  test("find") {
    check(Result.Dao.find(99L))
  }

  test("delete") {
    check(Result.Dao.delete(99L))
  }

  test("update") {
    check(Result.Dao.insert(Result(3L, 1L, 92,67,2)))
  }


}