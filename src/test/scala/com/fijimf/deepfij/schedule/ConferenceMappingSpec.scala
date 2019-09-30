package com.fijimf.deepfij.schedule

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfi.schedule.model.{Alias, ConferenceMapping, Result}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.{Colors, ExecutionContexts}
import org.scalatest.{FunSuite, Matchers}


class ConferenceMappingSpec extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
  )

  test("insert") {
    check(ConferenceMapping.Dao.insert(ConferenceMapping(0L, 2L, 3L, 4L)))
  }

  test("list") {
    check(ConferenceMapping.Dao.list())
  }

  test("find") {
    check(ConferenceMapping.Dao.find(99L))
  }

  test("delete") {
    check(ConferenceMapping.Dao.delete(99L))
  }

  test("update") {
    check(ConferenceMapping.Dao.update(ConferenceMapping(1L, 2L, 3L, 4L)))
  }
}