package com.fijimf.deepfij.schedule

import cats.effect.{ContextShift, IO}
import com.fijimf.deepfi.schedule.model.Team
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import doobie.util.{Colors, ExecutionContexts}
import org.scalatest.{FunSuite, Matchers}


class TeamSpec extends FunSuite with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val transactor: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql:deepfijdb", "fijuser", "mut()mb()"
  )

  test("insert") {
    check(Team.Dao.insert(Team(0L, "georgetown", "Georgetown", "Hoyas", "http://xxx.xxx.com/xxx/xxxxx/xxxxxxxx", "#AAFFDD", "blue")))
  }


  test("list") {
    check(Team.Dao.list())
  }

  test("find") {
    check(Team.Dao.find(99L))
  }

  test("delete") {
    check(Team.Dao.delete(99L))
  }

  test("update") {
    check(Team.Dao.update(Team(0L, "georgetown", "Georgetown", "Hoyas", "http://xxx.xxx.com/xxx/xxxxx/xxxxxxxx", "#AAFFDD", "blue")))
  }

}