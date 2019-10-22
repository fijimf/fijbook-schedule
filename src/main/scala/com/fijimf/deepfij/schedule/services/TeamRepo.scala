package com.fijimf.deepfij.schedule.services
import com.fijimf.deepfij.schedule.model.Team

trait TeamRepo[F[_]] {

  def insertTeam(t: Team): F[Team]

  def updateTeam(t: Team): F[Team]

  def deleteTeam(id: Long): F[Int]

  def listTeam(): F[List[Team]]

  def findTeam(id: Long): F[Option[Team]]
}
