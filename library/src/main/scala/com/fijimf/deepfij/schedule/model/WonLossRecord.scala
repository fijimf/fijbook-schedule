package com.fijimf.deepfij.schedule.model
import cats.implicits._

case class WonLossRecord(wins: Int, losses: Int) extends Ordered[WonLossRecord] {

  override def compare(r: WonLossRecord): Int = {
    val a: Int = (r.wins - r.losses) - (wins - losses)
    val b: Int = r.wins - wins
    if (a === 0) b else a
  }

  def addWin(): WonLossRecord = copy(wins + 1, losses)

  def addLoss(): WonLossRecord = copy(wins, losses + 1)
}

object WonLossRecord {
  def from(t: Team, grs: List[(Game, Option[Result])]): WonLossRecord = {
    grs.foldLeft(WonLossRecord(0, 0)) { case (wl: WonLossRecord, gr: (Game, Option[Result])) =>
      gr._2 match {
        case Some(res) =>
          if (res.homeScore > res.awayScore) {
            if (t.id === gr._1.homeTeamId) wl.addWin() else wl.addLoss()
          } else {
            if (t.id === gr._1.awayTeamId) wl.addWin() else wl.addLoss()
          }
        case None => wl
      }
    }
  }
}