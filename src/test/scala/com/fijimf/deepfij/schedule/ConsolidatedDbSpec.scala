package com.fijimf.deepfij.schedule

import cats.implicits._
import cats.effect.{Async, ContextShift, ExitCode, IO, IOApp, Resource, Timer}
import com.spotify.docker.client.messages.{ContainerConfig, ContainerCreation, ContainerState, HostConfig, PortBinding}
import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import java.util

import com.spotify.docker.client.DockerClient.ListContainersParam
import doobie.hikari.HikariTransactor
import doobie.util.{Colors, ExecutionContexts}
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.annotation.tailrec

object ConsolidatedDbSpec {

  override val colors: Colors.Ansi.type = doobie.util.Colors.Ansi // just for docs
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  val user = "fijuser"
  val password = "password"
  val database = "deepfijdb"
  val port = "7373"
  val driver = "org.postgresql.Driver"
  val url: String = s"jdbc:postgresql://localhost:$port/$database"



  val dockerClient: Resource[IO, DefaultDockerClient] =
    Resource.make(
      IO {
        DefaultDockerClient.fromEnv().build()
      }
    )(
      (d: DefaultDockerClient) => IO {
        d.close()
      }
    )

  def dockerContainer(docker: DockerClient): Resource[IO, String] = {
    val acquire: IO[String] = IO {
      docker.pull("postgres:latest")

      import scala.collection.JavaConverters._
      docker
        .listContainers(ListContainersParam.allContainers(true))
        .asScala
        .toList
        .filter(_.names().contains("deepfij-integration-testdb"))
        .foreach(c=>{
          println(s"Killing and removing ${c.id()}")
          docker.killContainer(c.id())
          docker.removeContainer(c.id())
        })

      val containerConfig: ContainerConfig = ContainerConfig
        .builder
        .hostConfig(createHostConfig)
        .image("postgres:latest")
        .exposedPorts(s"7373/tcp")
        .env(s"POSTGRES_USER=$user", s"POSTGRES_PASSWORD=$password", s"POSTGRES_DB=$database")
        .build
      val creation: ContainerCreation = docker.createContainer(containerConfig, "deepfij-integration-testdb")
      val id: String = creation.id
      docker.startContainer(id)

      @tailrec
      def readyCheck(id:String):Unit={
        val status: String = docker.inspectContainer(id).state().status()
        if (status.toLowerCase=!="running") {
          println(status)
          readyCheck(id)
        }
      }
      readyCheck(id)
      id
    }
    val free: String => IO[Unit] = (id: String) => IO {
//      docker.stopContainer(id, 15)
//      docker.removeContainer(id)
    }
    Resource.make(acquire)(free)
  }

  private def createHostConfig: HostConfig = {
    val hostPorts = new util.ArrayList[PortBinding]
    hostPorts.add(PortBinding.of("0.0.0.0", "7373"))

    val portBindings = new util.HashMap[String, util.List[PortBinding]]
    portBindings.put("5432/tcp", hostPorts)
    HostConfig.builder.portBindings(portBindings).build
  }

  val ztransactor: Resource[IO, HikariTransactor[IO]] =
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

  def run(args: List[String]): IO[ExitCode] = {
    dockerClient.use { docker =>
      dockerContainer(docker).use { _ =>
        ztransactor.use { xa =>
          for {
            _ <- initDB(xa)

           object Spec extends FunSuite with BeforeAndAfterAll with Matchers with doobie.scalatest.IOChecker
          } yield {
            ExitCode.Success
          }
        }
      }
    }
  }
}
