package com.fijimf.deepfij.schedule

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfi.schedule.model.{Alias, Conference, Result}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.{Colors, ExecutionContexts}
import org.scalatest.{FunSuite, Matchers}


class ConferenceSpec extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
  )

  test("insert") {
    check(Conference.Dao.insert(Conference(0L, "big-east", "Big East", "The Big East Conference",None)))
  }


  test("list") {
    check(Conference.Dao.list())
  }

  test("find") {
    check(Conference.Dao.find(99L))
  }

  test("delete") {
    check(Conference.Dao.delete(99L))
  }

  test("update") {
    check(Conference.Dao.update(Conference(1L,"big-east","Big East", "The Big East Conference", None)))
  }

}