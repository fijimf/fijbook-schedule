package com.fijimf.deepfij.schedule

import java.sql.{Connection, DriverManager}

import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp, Resource, Timer}
import com.typesafe.config.{Config, ConfigFactory}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._

object Main extends IOApp {
  val log: Logger =LoggerFactory.getLogger(Main.getClass)
  val config: IO[Config] = IO.delay(ConfigFactory.load())

  val resourceConf: Resource[IO, Config] = {
    val free: Config => IO[Unit] = (c: Config) => IO {}
    Resource.make(config)(free)
  }
  val transactor: Resource[IO, HikariTransactor[IO]] = createTransactor

  private def createTransactor: Resource[IO, HikariTransactor[IO]] = {
    for {
      cf <- resourceConf
      driver: String = cf.getString("fijbook.schedule.db.driver")
      url: String = cf.getString("fijbook.schedule.db.url")
      user: String = cf.getString("fijbook.schedule.db.user")
      password: String = cf.getString("fijbook.schedule.db.password")
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[IO]
      _ <- readyCheck(driver,url,user,password)
      xa <- HikariTransactor.newHikariTransactor[IO](driver, url, user, password, ce, te)
    } yield xa
  }

  def readyCheck(driver:String, url:String, user:String, password:String): Resource[IO,Connection] = {
    val ioa = IO {
      Class.forName(driver)
      DriverManager.getConnection(url, user, password)
    }
    val alloc: IO[Connection] =retryWithBackOff(ioa,100.milliseconds,5)
    val free: Connection=>IO[Unit]=(c:Connection)=>IO{c.close()}
    Resource.make(alloc)(free)
  }

  def retryWithBackOff[A](ioa: IO[A], initialDelay: FiniteDuration, maxRetries: Int)
                         (implicit timer: Timer[IO]): IO[A] = {
    IO.delay(println(s"Checking database connection. $maxRetries retries left. "))
    ioa.handleErrorWith { error =>
      if (maxRetries > 0)
        IO.sleep(initialDelay) *> retryWithBackOff(ioa, initialDelay * 2, maxRetries - 1)
      else
        IO.raiseError(error)
    }
  }

  def run(args: List[String]): IO[ExitCode] = {
    transactor.use { xa =>
      for {
        _ <- initDB(xa)
        port <- config.map(_.getInt("fijbook.schedule.port"))
        exitCode <- ScheduleServer
          .stream[IO](xa, port)
          .compile[IO, IO, ExitCode]
          .drain
          .as(ExitCode.Success)
      } yield {
        exitCode

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
