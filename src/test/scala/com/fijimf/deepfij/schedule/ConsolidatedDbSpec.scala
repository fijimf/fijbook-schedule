package com.fijimf.deepfij.schedule

import java.sql.DriverManager
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}
import java.util

import cats.effect.{ContextShift, IO, Resource}
import com.fijimf.deepfi.schedule.model._
import com.fijimf.deepfi.schedule.services.{ScheduleRepo, Updater}
import com.spotify.docker.client.DockerClient.ListContainersParam
import com.spotify.docker.client.messages.{ContainerConfig, ContainerCreation, HostConfig, PortBinding}
import com.spotify.docker.client.{DefaultDockerClient, DockerClient}
import doobie.hikari.HikariTransactor
import doobie.implicits._
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

      it("truncate should typecheck") {
        check(Game.Dao.truncate())
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

      it("truncate should typecheck") {
        check(Result.Dao.truncate())
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

      it("truncate should typecheck") {
        check(Season.Dao.truncate())
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

      it("truncate should typecheck") {
        check(Team.Dao.truncate())
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

      it("truncate should typecheck") {
        check(Alias.Dao.truncate())
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

      it("truncate should typecheck") {
        check(ConferenceMapping.Dao.truncate())
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

      it("truncate should typecheck") {
        check(Conference.Dao.truncate())
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
      it("should list conferences"){
        (for {
          conferenceList <- repo.listConferences()
        } yield {
          assert(conferenceList.size >= 0)
        }).unsafeRunSync()
      }

      it ("should insert a conference"){
        (for {
          conferenceList <- repo.listConferences()
          c<- repo.insertConference(Conference(0L, "big-east","Big East", "The Big East Conference", Some("http://Blah.blah.com/logo.png")))
          conferenceList1<-repo.listConferences()
        } yield {
          assert(c.id >0L)
          assert(conferenceList.size+1===conferenceList1.size)
          assert(conferenceList1.contains(c))
        }).unsafeRunSync()
      }

      it ("should find a conferences"){
        (for {
          c<- repo.insertConference(Conference(0L, "pac12","Pac-12", "The Pac-12 Conference", Some("http://Blah.blah.com/logo.png")))
          c1<-repo.findConference(c.id)
          cx<-repo.findConference(-999L)
        } yield {
          assert(c1===Some(c))
          assert(cx.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a conferences"){
        (for {
          c<- repo.insertConference(Conference(0L, "big12","Big-12", "The Big-12 Conference", Some("http://Blah.blah.com/logo.png")))
          c1<-repo.findConference(c.id)
          c2<-repo.updateConference(c.copy(name="Big Twelve"))
          c3<-repo.findConference(c.id)
        } yield {
          assert(c1===Some(c))
          assert(c3===Some(c2))
          assert(!(c===c2))
        }).unsafeRunSync()

      }
      it("should delete a conferences"){
        (for {
          c<- repo.insertConference(Conference(0L, "big-west","Big West", "The Big West Conference", Some("http://Blah.blah.com/logo.png")))
          c1<-repo.findConference(c.id)
          n<-repo.deleteConference(c.id)
          c3<-repo.findConference(c.id)
        } yield {
          assert(c1===Some(c))
          assert(n===1)
          assert(c3===None)
        }).unsafeRunSync()

      }

    }
    describe("ConferenceMapping ops"){
      it("should list conferenceMappings"){
        (for {
          cmList <- repo.listConferenceMappings()
        } yield {
          assert(cmList.size >= 0)
        }).unsafeRunSync()
      }

      it ("should insert a conferenceMapping"){
        (for {
          cmList <- repo.listConferenceMappings()
          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, 1L,2L,3L))
          cmList1<-repo.listConferenceMappings()
        } yield {
          assert(cm.id >0L)
          assert(cmList.size+1===cmList1.size)
          assert(cmList1.contains(cm))
        }).unsafeRunSync()
      }

      it ("should find a conferenceMapping"){
        (for {
          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, 1L,3L,3L))
          cm1<-repo.findConferenceMapping(cm.id)
          cmx<-repo.findConferenceMapping(-999L)
        } yield {
          assert(cm1===Some(cm))
          assert(cmx.isEmpty)
        }).unsafeRunSync()
      }

      it("should update a conferenceMapping"){
        (for {
          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, 1L,4L,3L))
          cm1<-repo.findConferenceMapping(cm.id)
          cm2<-repo.updateConferenceMapping(cm.copy(conferenceId=5L))
          cm3<-repo.findConferenceMapping(cm.id)
        } yield {
          assert(cm1===Some(cm))
          assert(cm3===Some(cm2))
          assert(!(cm===cm2))
        }).unsafeRunSync()
      }

      it("should delete a conferenceMapping"){
        (for {
          cm<- repo.insertConferenceMapping(ConferenceMapping(0L, 1L,5L,3L))
          cm1<-repo.findConferenceMapping(cm.id)
          n<-repo.deleteConferenceMapping(cm.id)
          cm3<-repo.findConferenceMapping(cm.id)
        } yield {
          assert(cm1===Some(cm))
          assert(n===1)
          assert(cm3===None)
        }).unsafeRunSync()
      }

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

      it("should update a season"){
        (for {
          s<- repo.insertSeason(Season(0L, 2012))
          s1<-repo.findSeason(s.id)
          s2<-repo.updateSeason(s.copy(year=2001))
          s3<-repo.findSeason(s.id)
        } yield {
          assert(s1===Some(s))
          assert(s3===Some(s2))
          assert(!(s===s2))
        }).unsafeRunSync()

      }
      it("should delete a season"){
        (for {
          s<- repo.insertSeason(Season(0L, 2011))
          s1<-repo.findSeason(s.id)
          n<-repo.deleteSeason(s.id)
          s3<-repo.findSeason(s.id)
        } yield {
          assert(s1===Some(s))
          assert(n===1)
          assert(s3===None)
        }).unsafeRunSync()

      }


    }
    describe("Team ops"){

    }
  }

  describe("Snapshotter"){


  }

  describe("Updater"){
    val repo: ScheduleRepo[IO] = new ScheduleRepo[IO](transactor)
    val updater: Updater[IO] = Updater(transactor)

    def updaterSetup(): Unit = {

      (for {
        _ <- Alias.Dao.truncate().run.transact(transactor)
        _ <- Team.Dao.truncate().run.transact(transactor)
        _ <- Game.Dao.truncate().run.transact(transactor)
        _ <- Result.Dao.truncate().run.transact(transactor)
        _ <- Season.Dao.truncate().run.transact(transactor)
        s <- repo.insertSeason(Season(0L, 2020))
        georgetown <- repo.insertTeam(Team(0L, "georgetown", "Georgetown", "Hoyas", "", "", ""))
        harvard <- repo.insertTeam(Team(0L, "harvard", "Harvard", "Crimson", "", "", ""))
        duke <- repo.insertTeam(Team(0L, "duke", "Duke", "Blue Devils", "", "", ""))
        carolina <- repo.insertTeam(Team(0L, "north-carolina", "North Carolina", "Tar Heels", "", "", ""))
        villanova <- repo.insertTeam(Team(0L, "villanova", "Villanove", "Wildcats", "", "", ""))
        _ <- repo.insertAlias(Alias(0L, carolina.id, "unc"))
      } yield {

      }).unsafeRunSync
    }


    it("should initially have no games") {
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
      }).unsafeRunSync()
    }

    it("should lookup teams by key") {
      updaterSetup()
      import cats.implicits._
      (for {
        teams <- repo.listTeam()
        list <- teams.map(t => updater.loadTeam(t.key).value.map(_.toList)).sequence
      } yield {
        assert(teams === list.flatten)
      }).unsafeRunSync()
    }

    it("should lookup teams by alias") {
      updaterSetup()
      import cats.implicits._
      (for {
        aliases <- repo.listAliases()
        list <- aliases.map(a => updater.loadTeam(a.alias).value.map(_.toList)).sequence
      } yield {
        val aliasTeamIds: List[Long] = aliases.map(_.teamId)
        val teamIds: List[Long] = list.flatten.map(_.id)
        assert(!(aliasTeamIds =!= teamIds))
      }).unsafeRunSync()
    }

    it("should not find bogus teams") {
      updaterSetup()
      import cats.implicits._
      val strings = List("junk", "don't find me")
      (for {
        list <- strings.map(s => updater.loadTeam(s).value.map(_.toList)).sequence
      } yield {
        assert(list.flatten.isEmpty)
      }).unsafeRunSync()
    }

    it("should identify correct gamekeys") {

      val time: LocalDateTime = LocalDateTime.of(2019, 11, 15, 19, 30)
      val loadKey: String = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val updates = List(
        UpdateCandidate(time, "georgetown", "duke", Some("Verizon Center"), Some(false), None, None, None)
      )
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
        aliases <- repo.listAliases()
        keyMap <- updater.findKeys(updates, loadKey)
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
        assert(aliases.size === 1)
        assert(keyMap.size ===1 )
        keyMap.headOption.foreach{case (k,(g,or))=>{
          assert(teams.find(_.id===k.homeTeamId).map(_.nickname)===Some("Hoyas"))
          assert(teams.find(_.id===k.awayTeamId).map(_.nickname)===Some("Blue Devils"))
          assert(k.seasonId>0L)
        }}
      }).unsafeRunSync()
    }

    it("insert 1 new game") {

      val time: LocalDateTime = LocalDateTime.of(2019, 11, 15, 19, 30)
      val loadKey: String = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val updates = List(
        UpdateCandidate(time, "georgetown", "duke", Some("Verizon Center"), Some(false), None, None, None)
      )
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
        aliases <- repo.listAliases()
        changes <- updater.updateGamesAndResults(updates, loadKey)
        gamesPost <- repo.listGame()
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
        assert(aliases.size === 1)
        assert(changes.size ===1 )
        assert(gamesPost.size ===1 )
      }).unsafeRunSync()
    }

   it("insert 1 new game then delete it") {

      val time: LocalDateTime = LocalDateTime.of(2019, 11, 15, 19, 30)
      val loadKey: String = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
      val updates = List(
        UpdateCandidate(time, "georgetown", "duke", Some("Verizon Center"), Some(false), None, None, None)
      )
      updaterSetup()
      (for {
        games <- repo.listGame()
        teams <- repo.listTeam()
        aliases <- repo.listAliases()
        changes <- updater.updateGamesAndResults(updates, loadKey)
        gamesPostAdd <- repo.listGame()
        changes2 <- updater.updateGamesAndResults(List.empty[UpdateCandidate], loadKey)
        gamesPostDelete <- repo.listGame()
      } yield {
        assert(games.isEmpty)
        assert(teams.size === 5)
        assert(aliases.size === 1)
        assert(changes.size ===1 )
        assert(gamesPostAdd.size ===1 )
        assert(gamesPostDelete.isEmpty )
      }).unsafeRunSync()
    }

  }
}
