package com.fijimf.deepfij.schedule.model

import java.time.LocalDate

import cats.implicits._

case class ScheduleRoot
(
  teams: List[Team],
  conferences: List[Conference],
  seasons: List[Season],
  conferenceMappings: List[ConferenceMapping],
  games: List[Game],
  results: List[Result]
) {

  val teamById: Map[Long, Team] = teams.map(t => t.id -> t).toMap
  val teamByKey: Map[String, Team] = teams.map(t => t.key -> t).toMap
  val conferenceById: Map[Long, Conference] = conferences.map(c => c.id -> c).toMap
  val conferenceByKey: Map[String, Conference] = conferences.map(c => c.key -> c).toMap
  val seasonById: Map[Long, Season] = seasons.map(s => s.id -> s).toMap
  val seasonByYear: Map[Int, Season] = seasons.map(s => s.year -> s).toMap
  val gamesBySeason: Long => List[Game] = games.groupBy(_.seasonId).withDefaultValue(List.empty[Game])
  val teamsByConference: Map[Season, Map[Conference, List[Team]]] = {
    conferenceMappings.groupBy(_.seasonId).flatMap { case (seasonId: Long, m1: List[ConferenceMapping]) => {
      seasonById.get(seasonId).map(s => {
        s -> m1.groupBy(_.conferenceId).flatMap { case (conferenceId: Long, m2: List[ConferenceMapping]) =>
          conferenceById.get(conferenceId).map(c => {
            c -> m2.flatMap(cm => teamById.get(cm.teamId))
          })
        }
      }).toMap
    }
    }
  }

  def dateToSeason(d:LocalDate):Option[Season] = {
    val y = d.getMonthValue match {
      case m if m<6 => d.getYear
      case m if m>=6 => d.getYear+1
    }
    seasons.find(_.year===y).orElse(seasons.lastOption)
  }


  def isConferenceGame(g: Game): Boolean = {
    (for {
      c1 <- conferenceMappings.find(c => c.seasonId === g.seasonId && c.teamId === g.homeTeamId)
      c2 <- conferenceMappings.find(c => c.seasonId === g.seasonId && c.teamId === g.awayTeamId)
    } yield {
      c1.conferenceId === c2.conferenceId
    }).getOrElse(false)
  }


  def isPlaying(t: Team, g: Game): Boolean = g.homeTeamId === t.id || g.awayTeamId === t.id

  def isWinner(t: Team, g: Game, r: Option[Result]): Option[Boolean] = r match {
    case Some(r) if r.homeScore > r.awayScore => Some(t.id === g.homeTeamId)
    case Some(r) if r.homeScore < r.awayScore => Some(t.id === g.awayTeamId)
    case None => None
  }

  def isLoser(t: Team, g: Game, r: Option[Result]): Option[Boolean] = r match {
    case Some(r) if r.homeScore < r.awayScore => Some(t.id === g.homeTeamId)
    case Some(r) if r.homeScore > r.awayScore => Some(t.id === g.awayTeamId)
    case None => None
  }
}

case class TeamPage
(
  t:Team,
  s:Season,
  d:LocalDate,
  games:List[(Game,Option[Result])],
  conferenceStandings:ConferenceStandings,
  overallRecord:WonLossRecord,
  conferenceRecord:WonLossRecord
)

case class ConferenceStandings(conference: Conference, rows: List[StandingsRow])

case class StandingsRow(t: Team, conferenceRecord: WonLossRecord, overallRecord: WonLossRecord) extends Ordered[StandingsRow] {
  override def compare(r: StandingsRow): Int = {
    val a: Int = conferenceRecord.compare(r.conferenceRecord)
    val b: Int = overallRecord.compare(r.overallRecord)
    val c: Int = t.name.compare(r.t.name)
    if (a === 0) {
      if (b === 0) {
        c
      } else {
        b
      }
    } else {
      a
    }
  }
}

case class WonLossRecord(wins: Int, losses: Int) extends Ordered[WonLossRecord] {

  override def compare(r: WonLossRecord): Int = {
    val a: Int = (r.wins - r.losses) - (wins - losses)
    val b: Int = r.wins - wins
    if (a === 0) b else a
  }
}

