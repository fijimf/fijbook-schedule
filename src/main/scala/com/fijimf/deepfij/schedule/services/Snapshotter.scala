package com.fijimf.deepfij.schedule.services

import cats.effect.{Resource, Sync}
import cats.implicits._
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.model.S3ObjectInputStream
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.util.IOUtils
import com.fijimf.deepfij.schedule.model._
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._

class Snapshotter[F[_]](xa: Transactor[F])(implicit F: Sync[F]) {
  val repo: ScheduleRepo[F] = new ScheduleRepo[F](xa)


  private def createClient(): AmazonS3 = {
    AmazonS3ClientBuilder.standard()
      .withCredentials(new DefaultAWSCredentialsProviderChain())
      .withEndpointConfiguration(new EndpointConfiguration("s3.amazonaws.com", "us-east-1"))
      .build()
  }


  def writeStaticData(bucket: String, key: String): F[Unit] = {
    for {
      data <- snapshotStaticData()
    } yield {
      createClient().putObject(bucket, key, data)
    }
  }

  def insertTeams(teams: List[TeamRecord]): F[Map[String, Team]] = {
    for {
      ts <- teams.map(tr => Team.Dao.insert(tr.toTeam).withUniqueGeneratedKeys[Team](Team.Dao.cols: _*).transact(xa)).sequence
    } yield {
      ts.map(t => t.key -> t).toMap
    }
  }

  def insertAliases(aliases: List[AliasRecord], teamMap: Map[String, Team]): F[List[Alias]] = {
    for {
      as <- aliases.flatMap(ar => {
        teamMap.get(ar.teamKey) match {
          case Some(t) => List(ar.toAlias(t.id))
          case None => List.empty[Alias]
        }
      }).map(a => Alias.Dao.insert(a).withUniqueGeneratedKeys[Alias](Alias.Dao.cols: _*).transact(xa)).sequence
    } yield {
      as
    }
  }

  def insertConferences(conferences: List[ConferenceRecord]): F[List[Conference]] = {
    for {
      cs <- conferences.map(cr => Conference.Dao.insert(cr.toConference).withUniqueGeneratedKeys[Conference](Conference.Dao.cols: _*).transact(xa)).sequence
    } yield {
      cs
    }
  }

  def readStaticData(bucket: String, key: String): F[Unit] = {
    val s3is: Resource[F, S3ObjectInputStream] = Resource.make[F, S3ObjectInputStream] {
      F.delay(createClient().getObject(bucket, key).getObjectContent)
    } { is: S3ObjectInputStream => F.pure(is.close()) }

    s3is.use(is => {
      val value: Either[circe.Error, StaticScheduleData] = decode[StaticScheduleData](IOUtils.toString(is))
      value match {
        case Left(err: circe.Error) => F.pure {}
        case Right(data: StaticScheduleData) =>
          for {
            _ <- Team.Dao.truncate().run.transact(xa)
            _ <- Alias.Dao.truncate().run.transact(xa)
            _ <- Conference.Dao.truncate().run.transact(xa)
            teamMap <- insertTeams(data.teams)
            aliasList <- insertAliases(data.aliases, teamMap)
            conferenceList <- insertConferences(data.conferences)
          } yield {}
      }
    })
  }

  def writeSeasonalData(bucket:String, key:String):F[Unit] = ??? /* Season ConferenceMapping Game Result */
  def readSeasonalData(bucket:String, key:String):F[Unit] = ???

  def snapshotStaticData(): F[String] =
    for {
      teams <- repo.listTeam()
      aliases <- repo.listAliases()
      conferences <- repo.listConferences()
    } yield {
      val teamRecords: List[TeamRecord] = teams.map(_.toSnapshotRecord)
      val aliasRecords: List[AliasRecord] = aliases.flatMap(a => teams.find(t => a.teamId === t.id).toList.map(t => AliasRecord(t.key, a.alias)))
      val conferenceRecords: List[ConferenceRecord] = conferences.map(_.toSnapshotRecord())
      StaticScheduleData(teamRecords, aliasRecords, conferenceRecords).asJson.spaces2
    }
}
