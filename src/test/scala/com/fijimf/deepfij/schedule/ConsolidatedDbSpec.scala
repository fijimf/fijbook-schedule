package com.fijimf.deepfij.schedule

import java.sql.DriverManager
import java.time.{LocalDate, LocalDateTime}
import java.util

import cats.effect.{ContextShift, IO, Resource}
import com.fijimf.deepfi.schedule.model.{Game, Result}
import com.spotify.docker.client.DockerClient.ListContainersParam
import com.spotify.docker.client.messages.{ContainerConfig, ContainerCreation, HostConfig, PortBinding}
import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import doobie.hikari.HikariTransactor
import doobie.util.{Colors, ExecutionContexts}
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class ConsolidatedDbSpec extends FunSuite with BeforeAndAfterAll with Matchers with doobie.scalatest.IOChecker {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val user = "fijuser"
  val password = "password"
  val database = "deepfijdb"
  val port = "7373"
  val driver = "org.postgresql.Driver"
  val url: String = s"jdbc:postgresql://localhost:$port/$database"


  def dockerClient(): IO[DefaultDockerClient] = IO {
    DefaultDockerClient.fromEnv().build()
  }

  def createDockerContainer(docker: DockerClient): IO[String] = IO {
    docker.pull("postgres:latest")
    docker
      .listContainers(ListContainersParam.allContainers(true))
      .asScala
      .toList
      .filter(_.names().contains("deepfij-integration-testdb"))
      .foreach(c => {
        println(s"Killing and removing ${c.id()}")
        docker.killContainer(c.id())
        docker.removeContainer(c.id())
      })

    val hostConfig: HostConfig = {
      val hostPorts = new util.ArrayList[PortBinding]
      hostPorts.add(PortBinding.of("0.0.0.0", "7373"))

      val portBindings = new util.HashMap[String, util.List[PortBinding]]
      portBindings.put("5432/tcp", hostPorts)
      HostConfig.builder.portBindings(portBindings).build
    }

    val containerConfig: ContainerConfig = ContainerConfig
      .builder
      .hostConfig(hostConfig)
      .image("postgres:latest")
      .exposedPorts(s"7373/tcp")
      .env(s"POSTGRES_USER=$user", s"POSTGRES_PASSWORD=$password", s"POSTGRES_DB=$database")
      .build
    val creation: ContainerCreation = docker.createContainer(containerConfig, "deepfij-integration-testdb")
    val id: String = creation.id
    docker.startContainer(id)

    @tailrec
    def readyCheck(): Unit = {
      Try {
        Class.forName(driver)
        DriverManager.getConnection(url, user, password)
      } match {
        case Success(c) => c.close()
        case Failure(_) =>
          Thread.sleep(250)
          readyCheck()
      }
    }

    readyCheck()
    id
  }

  def cleanUpDockerContainer(docker: DockerClient, containerId: String): IO[Unit] = IO {
    docker.killContainer(containerId)
    docker.removeContainer(containerId)
  }

  val txResource: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      te <- ExecutionContexts.cachedThreadPool[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        driver,
        url,
        user,
        password,
        ce,
        te
      )
    } yield xa

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  val (transactor, dbClose, containerId) = {
    for {
      d <- dockerClient()
      container <- createDockerContainer(d)
      (xa, close) <- txResource.allocated
    } yield {
      (xa, close, container)
    }
    }.unsafeRunSync()

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

  override def beforeAll(): Unit = {
    initDB(transactor).unsafeRunSync()
  }

  override def afterAll(): Unit = {
    for {
      _ <- dbClose
      d <- dockerClient()
      _ <- cleanUpDockerContainer(d, containerId)
    } yield {

    }
    }.unsafeRunSync()

  test("Insert game") {
    check(Game.Dao.insert(Game(0L, 1L, LocalDate.now(), LocalDateTime.now(), 5L, 7L, Some("MCI Center"), None, "20110302")))
  }

  test("List games") {
    check(Game.Dao.list())
  }

  test("Find game") {
    check(Game.Dao.find(99L))
  }

  test("FindByLoadKey") {
    check(Game.Dao.findByLoadKey("20190923"))
  }

  test("Delete Game") {
    check(Game.Dao.delete(99L))
  }

  test("Update Game") {
    check(Game.Dao.update(Game(83L, 1L, LocalDate.now(), LocalDateTime.now(), 5L, 7L, Some("MCI Center"), None, "20190911")))
  }

  test("Insert Result") {
    check(Result.Dao.insert(Result(0L, 1L, 92, 67, 2)))
  }

  test("List Result") {
    check(Result.Dao.list())
  }

  test("Find Result") {
    check(Result.Dao.find(99L))
  }

  test("Delete Result") {
    check(Result.Dao.delete(99L))
  }

  test("DeleteByGameId") {
    check(Result.Dao.deleteByGameId(99L))
  }

  test("Update Result") {
    check(Result.Dao.insert(Result(3L, 1L, 92, 67, 2)))
  }


}
