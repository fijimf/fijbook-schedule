package com.fijimf.deepfij.schedule.model

import java.time.LocalDate

import cats.Eq
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

  implicit val eqFoo: Eq[LocalDate] = Eq.fromUniversalEquals
  val teamById: Map[Long, Team] = teams.map(t => t.id -> t).toMap
  val teamByKey: Map[String, Team] = teams.map(t => t.key -> t).toMap
  val conferenceById: Map[Long, Conference] = conferences.map(c => c.id -> c).toMap
  val conferenceByKey: Map[String, Conference] = conferences.map(c => c.key -> c).toMap
  val conferenceMapping: Map[Long, Map[Long, Conference]] = {
    conferenceMappings
      .groupBy(_.seasonId)
      .mapValues(cms => {
        cms.flatMap(cm => {
          conferenceById.get(cm.conferenceId)
            .map(conf => cm.teamId -> conf)
        }).toMap
      })
  }
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
  val resultsByGame: Map[Long, Result] = results.map(r => r.gameId -> r).toMap

  def gamesForTeam(t: Team, s: Season): List[Game] = {
    games.filter(g => isPlaying(t, g) && g.seasonId === s.id)
  }

  def gamesForTeamWithResults(t: Team, s: Season): List[(Game, Option[Result])] = {
    withResults(gamesForTeam(t, s))
  }

  def withResults(gs: List[Game]): List[(Game, Option[Result])] = gs.map(g => g -> resultsByGame.get(g.id))

  def conferenceStandings(c: Conference, s: Season): ConferenceStandings = {
    ConferenceStandings(c, teamsByConference(s)(c).map(t => {
      val teamGames: List[(Game, Option[Result])] = withResults(gamesForTeam(t, s))
      val confRecord: WonLossRecord = WonLossRecord.from(t, teamGames.filter(q => isConferenceGame(q._1)))
      val overallRecord: WonLossRecord = WonLossRecord.from(t, teamGames)
      StandingsRow(t, confRecord, overallRecord)
    }))
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


  def today():LocalDate = LocalDate.now()

  def currentSeason():Option[Season]=dateToSeason(today())

  def gamesForDate(date:LocalDate):List[Game]= {
    dateToSeason(date).map(s=>gamesBySeason(s.id).filter(_.date === date)).getOrElse(List.empty[Game])
  }

  def gamesForDateWithResults(date:LocalDate) = withResults(gamesForDate(date))
}

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

  def addWin: WonLossRecord = copy(wins + 1, losses)

  def addLoss: WonLossRecord = copy(wins, losses + 1)
}

object WonLossRecord {
  def from(t: Team, grs: List[(Game, Option[Result])]): WonLossRecord = {
    grs.foldLeft(WonLossRecord(0, 0)) { case (wl: WonLossRecord, gr: (Game, Option[Result])) =>
      gr._2 match {
        case Some(res) =>
          if (res.homeScore > res.awayScore) {
            if (t.id === gr._1.homeTeamId) wl.addWin else wl.addLoss
          } else {
            if (t.id === gr._1.awayTeamId) wl.addWin else wl.addLoss
          }
        case None => wl
      }
    }
  }
}
