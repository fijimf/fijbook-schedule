package com.fijimf.deepfij.schedule.model

import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.commons.codec.digest.DigestUtils


final case class StaticScheduleData(teams: List[TeamRecord], aliases: List[AliasRecord], conferences: List[ConferenceRecord])

final case class TeamRecord(key: String, name: String, nickname: String, logoUrl: String, color1: String, color2: String) {
  def toTeam: Team =Team(0L,key,name,nickname,logoUrl, color1, color2)
}

final case class AliasRecord(teamKey: String, alias: String) {
  def toAlias(teamId:Long): Alias =Alias(0L, teamId, alias)
}

final case class ConferenceRecord(key: String, name: String, longName: String, logoUrl: Option[String]) {
  def toConference: Conference =Conference(0L, key, name, longName, logoUrl)
}

object StaticScheduleData {
  def digest(data:StaticScheduleData):String= {
    val json: Json = data.asJson
    val spaces: String = json.spaces2
    DigestUtils.md5Hex(spaces)
  }
}


