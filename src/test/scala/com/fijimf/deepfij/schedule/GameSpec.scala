package com.fijimf.deepfij.schedule

import java.time.{LocalDate, LocalDateTime}

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfi.schedule.model.Game
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.{Colors, ExecutionContexts}
import org.scalatest.{FunSuite, Matchers}


class GameSpec extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
  )

  test("insert") {
    check(Game.Dao.insert(Game(0L, 1L, LocalDate.now(), LocalDateTime.now(), 5L, 7L, Some("MCI Center"), None,"20110302")))
  }

  test("list") {
    check(Game.Dao.list())
  }

  test("find") {
    check(Game.Dao.find(99L))
  }

  test("findByDateTeams") {
    check(Game.Dao.findByDateTeams(LocalDate.now(), 1L,2L))
  }
  test("findByLoadKey") {
    check(Game.Dao.findByLoadKey("20190923"))
  }

  test("delete") {
    check(Game.Dao.delete(99L))
  }

  test("update") {
    check(Game.Dao.update(Game(83L, 1L, LocalDate.now(), LocalDateTime.now(), 5L, 7L, Some("MCI Center"), None,"20190911")))
  }
}