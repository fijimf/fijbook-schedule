package com.fijimf.deepfij.schedule.services
import com.fijimf.deepfij.schedule.model.Season

trait SeasonRepo[F[_]] {

  def insertSeason(s: Season): F[Season]

  def updateSeason(s: Season): F[Season]

  def deleteSeason(id: Long): F[Int]

  def listSeason(): F[List[Season]]

  def findSeason(id: Long): F[Option[Season]]
}
