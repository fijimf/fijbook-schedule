package com.fijimf.deepfij.schedule

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfi.schedule.model.{Alias, ConferenceMapping}
import doobie.util.{Colors, ExecutionContexts}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import org.scalatest.{FunSuite, Matchers}


class AliasSpec extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
  )

  test("insert") {
    check(Alias.Dao.insert(Alias(0L, 2L, "st. johns")))
  }

  test("list") {
    check(Alias.Dao.list())
  }

  test("find") {
    check(Alias.Dao.find(99L))
  }

  test("delete") {
    check(Alias.Dao.delete(99L))
  }

  test ("update") {
    check(Alias.Dao.update(Alias(3L,4L,"ssssss")))
  }

}