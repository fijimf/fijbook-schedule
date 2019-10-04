package com.fijimf.deepfij.schedule

import java.time.{LocalDate, LocalDateTime}

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfi.schedule.model.{Game, Season, Team}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.{Colors, ExecutionContexts}
import org.scalatest.{FunSuite, Matchers}


class SeasonSpec extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
  )

  test("insert") {
    check(Season.Dao.insert(Season(0L, 1999)))
  }


  test("list") {
    check(Season.Dao.list())
  }

  test("find") {
    check(Season.Dao.find(99L))
  }

  test("findByDate") {
    check(Season.Dao.findByDate(LocalDateTime.now()))
  }

  test("delete") {
    check(Season.Dao.delete(99L))
  }

  test("update") {
    check(Season.Dao.update(Season(3L, 1999)))
  }


}