package com.fijimf.deepfij.schedule.util

import cats.Applicative
import com.fijimf.deepfij.schedule.BuildInfo
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

case class ServerInfo(name: String, version: String, scalaVersion: String, sbtVersion: String, buildNumber: Int, builtAt: String, isOk: Boolean)

case object ServerInfo {

  implicit val healthyEncoder: Encoder.AsObject[ServerInfo] = deriveEncoder[ServerInfo]
  implicit def healthyEntityEncoder[F[_] : Applicative]: EntityEncoder[F, ServerInfo] = jsonEncoderOf

  def fromStatus(status: Boolean): ServerInfo = {
    ServerInfo(
      BuildInfo.name,
      BuildInfo.version,
      BuildInfo.scalaVersion,
      BuildInfo.sbtVersion,
      BuildInfo.buildInfoBuildNumber,
      BuildInfo.builtAtString,
      status)
  }
}