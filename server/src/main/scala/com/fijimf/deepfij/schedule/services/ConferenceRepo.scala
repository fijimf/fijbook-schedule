package com.fijimf.deepfij.schedule.services
import cats.effect.Sync
import com.fijimf.deepfij.schedule.model.Conference

trait ConferenceRepo[F[_]] {

  def insertConference(c: Conference): F[Conference]

  def updateConference(c: Conference): F[Conference]

  def deleteConference(id: Long): F[Int]

  def listConferences(): F[List[Conference]]

  def findConference(id: Long): F[Option[Conference]]
}
