package com.fijimf.deepfij.schedule

import java.sql.DriverManager
import java.time.{LocalDate, LocalDateTime}
import java.util

import cats.effect.{ContextShift, IO, Resource}
import com.fijimf.deepfi.schedule.model._
import com.fijimf.deepfi.schedule.services.ScheduleRepo
import com.spotify.docker.client.DockerClient.ListContainersParam
import com.spotify.docker.client.messages.{ContainerConfig, ContainerCreation, HostConfig, PortBinding}
import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import doobie.hikari.HikariTransactor
import doobie.util.{Colors, ExecutionContexts}
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, FunSpec, Matchers}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class ConsolidatedDbSpec extends FunSpec with BeforeAndAfterAll with Matchers with doobie.scalatest.IOChecker {

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

  describe("Doobie typechecking Dao's") {
    describe("Game.Dao") {
      it("insert should typecheck") {
        check(Game.Dao.insert(Game(0L, 1L, LocalDate.now(), LocalDateTime.now(), 5L, 7L, Some("MCI Center"), None, "20110302")))
      }

      it("list should typecheck") {
        check(Game.Dao.list())
      }

      it("find should typecheck") {
        check(Game.Dao.find(99L))
      }

      it("findByLoadKey should typecheck") {
        check(Game.Dao.findByLoadKey("20190923"))
      }

      it("delete should typecheck") {
        check(Game.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Game.Dao.update(Game(83L, 1L, LocalDate.now(), LocalDateTime.now(), 5L, 7L, Some("MCI Center"), None, "20190911")))
      }
    }
    describe("Result.Dao") {

      it("insert should typecheck") {
        check(Result.Dao.insert(Result(0L, 1L, 92, 67, 2)))
      }

      it("list should typecheck") {
        check(Result.Dao.list())
      }

      it("find should typecheck") {
        check(Result.Dao.find(99L))
      }

      it("delete should typecheck") {
        check(Result.Dao.delete(99L))
      }

      it("deleteByGameId should typecheck") {
        check(Result.Dao.deleteByGameId(99L))
      }

      it("update should typecheck") {
        check(Result.Dao.insert(Result(3L, 1L, 92, 67, 2)))
      }
    }
    describe("Season.Dao") {

      it("insert should typecheck") {
        check(Season.Dao.insert(Season(0L, 1999)))
      }


      it("list should typecheck") {
        check(Season.Dao.list())
  }

      it("find should typecheck") {
        check(Season.Dao.find(99L))
      }

      it("findByDate should typecheck") {
        check(Season.Dao.findByDate(LocalDateTime.now()))
      }

      it("delete should typecheck") {
        check(Season.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Season.Dao.update(Season(3L, 1999)))
      }
    }
    describe("Team.Dao") {

      it("insert should typecheck") {
        check(Team.Dao.insert(Team(0L, "georgetown", "Georgetown", "Hoyas", "http://xxx.xxx.com/xxx/xxxxx/xxxxxxxx", "#AAFFDD", "blue")))
      }


      it("list should typecheck") {
        check(Team.Dao.list())
      }

      it("find should typecheck") {
        check(Team.Dao.find(99L))
      }
      it("findByAlias should typecheck") {
        check(Team.Dao.findByAlias("southern-cal"))
      }

      it("findByKey should typecheck") {
        check(Team.Dao.findByKey("usc"))
      }

      it("delete should typecheck") {
        check(Team.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Team.Dao.update(Team(0L, "georgetown", "Georgetown", "Hoyas", "http://xxx.xxx.com/xxx/xxxxx/xxxxxxxx", "#AAFFDD", "blue")))
      }
    }
    describe("Alias.Dao") {

      it("insert should typecheck") {
        check(Alias.Dao.insert(Alias(0L, 2L, "st. johns")))
      }

      it("list should typecheck") {
        check(Alias.Dao.list())
      }

      it("find should typecheck") {
        check(Alias.Dao.find(99L))
      }

      it("delete should typecheck") {
        check(Alias.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Alias.Dao.update(Alias(3L, 4L, "ssssss")))
      }

    }
    describe("ConferenceMapping.Dao") {

      it("insert should typecheck") {
        check(ConferenceMapping.Dao.insert(ConferenceMapping(0L, 2L, 3L, 4L)))
      }

      it("list should typecheck") {
        check(ConferenceMapping.Dao.list())
      }

      it("find should typecheck") {
        check(ConferenceMapping.Dao.find(99L))
      }

      it("delete should typecheck") {
        check(ConferenceMapping.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(ConferenceMapping.Dao.update(ConferenceMapping(1L, 2L, 3L, 4L)))
      }
    }

    describe("Conference.Dao") {

      it("insert should typecheck") {
        check(Conference.Dao.insert(Conference(0L, "big-east", "Big East", "The Big East Conference", None)))
      }

      it("list should typecheck") {
        check(Conference.Dao.list())
      }

      it("find should typecheck") {
        check(Conference.Dao.find(99L))
      }

      it("delete should typecheck") {
        check(Conference.Dao.delete(99L))
      }

      it("update should typecheck") {
        check(Conference.Dao.update(Conference(1L, "big-east", "Big East", "The Big East Conference", None)))
      }
    }
  }

  describe("Schedule repo ops") {
    val repo = new ScheduleRepo[IO](transactor)
    describe("Alias ops") {
      it("should list all aliases") {
        (for {
          aliasList <- repo.listAliases()
        } yield {
          assert(aliasList.size >= 0)
        }).unsafeRunSync()
      }

      it("should insert an alias") {
        (for {
          aliasList0 <- repo.listAliases()
          a <- repo.insertAlias(Alias(0L, 32L, "st. johns"))
          aliasList1 <- repo.listAliases()
        } yield {
          assert(a.id > 0L)
          assert(aliasList1.size >= aliasList0.size)
          assert(aliasList1.contains(a))
        }).unsafeRunSync()
      }

      it("should find aliases") {
        (for {
          a <- repo.insertAlias(Alias(0L, 32L, "st. francis"))
          a1 <- repo.findAlias(a.id)
          ax <- repo.findAlias(-999L)
        } yield {
          assert(a1 === Some(a))
          assert(ax.isEmpty)
        }).unsafeRunSync()
      }

      it("should update an alias") {
        (for {
          a <- repo.insertAlias(Alias(0L, 32L, "usc"))
          a1 <- repo.findAlias(a.id)
          a2 <- repo.updateAlias(a.copy(alias = "southern cal"))
          a3 <- repo.findAlias(a.id)
        } yield {
          assert(a1 === Some(a))
          assert(!(a === a2))
          assert(a3 === Some(a2))
        }).unsafeRunSync()
      }

      it("should delete an alias") {
        (for {
          a <- repo.insertAlias(Alias(0L, 32L, "villanova"))
          list1 <- repo.listAliases()
          n <- repo.deleteAlias(a.id)
          list2 <- repo.listAliases()
          m <- repo.deleteAlias(-999L)
        } yield {
          assert(list1.contains(a))
          assert(n === 1)
          assert(!list2.contains(a))
          assert(m === 0)
        }).unsafeRunSync()
      }
    }
    describe("Conference ops"){

    }
    describe("ConferenceMapping ops"){

    }
    describe("Game ops"){

    }
    describe("Result ops"){

    }
    describe("Season ops"){

      it("should list seasons"){
        (for {
          seasonList <- repo.listSeason()
        } yield {
          assert(seasonList.size >= 0)
        }).unsafeRunSync()
      }

      it ("should insert a season"){
        (for {
          seasonList <- repo.listSeason()
          s<- repo.insertSeason(Season(0L, 2014))
          seasonList1<-repo.listSeason()
        } yield {
          assert(s.id >0L)
          assert(seasonList.size+1===seasonList1.size)
          assert(seasonList1.contains(s))
        }).unsafeRunSync()
      }

      it ("should find a season"){
        (for {
          s<- repo.insertSeason(Season(0L, 2013))
          s1<-repo.findSeason(s.id)
          sx<-repo.findSeason(-999L)
        } yield {
          assert(s1===Some(s))
          assert(sx.isEmpty)
        }).unsafeRunSync()
      }


    }
    describe("Team ops"){

    }
  }

  describe("Snapshotter"){

  }

  describe("Updater"){

  }
}
