package com.fijimf.deepfi.schedule

import cats.effect.{ExitCode, IO, IOApp, Resource}
import com.typesafe.config.{Config, ConfigFactory}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway

object Main extends IOApp {
  val zzz: Config = ConfigFactory.load("application.conf")
  println(zzz)
  val conf: Resource[IO, Config] = {
    val alloc: IO[Config] = IO.delay(ConfigFactory.load("application.conf"))
    val free: Config => IO[Unit] = (c: Config) => IO {}
    Resource.make(alloc)(free)
  }

  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      cf <- conf
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        cf.getString("fijbook.schedule.db.driver"),
        cf.getString("fijbook.schedule.db.url"),
        cf.getString("fijbook.schedule.db.user"),
        cf.getString("fijbook.schedule.db.password"),
        ce,
        te
      )
    } yield xa


  def run(args: List[String]): IO[ExitCode] = {
    transactor.use { xa =>
      for {
        _ <- initDB(xa)
      } yield {
        ExitCode.Success
      }
    }
  }


  def initDB(xa: HikariTransactor[IO]): IO[Int] = {
    xa.configure { dataSource =>
      IO {
        Flyway
          .configure()
          .dataSource(dataSource)
          .locations("classpath:db/migration")
          .baselineOnMigrate(true)
          .table("flyway_schema_history_schedule")
          .load()
          .migrate()
      }
    }
  }
}
