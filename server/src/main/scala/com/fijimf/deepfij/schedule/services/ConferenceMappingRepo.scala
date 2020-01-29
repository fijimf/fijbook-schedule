package com.fijimf.deepfij.schedule.services
import com.fijimf.deepfij.schedule.model.ConferenceMapping

trait ConferenceMappingRepo[F[_]] {

  def insertConferenceMapping(cm: ConferenceMapping): F[ConferenceMapping]

  def updateConferenceMapping(cm: ConferenceMapping): F[ConferenceMapping]

  def deleteConferenceMapping(id: Long): F[Int]

  def listConferenceMappings(): F[List[ConferenceMapping]]

  def findConferenceMapping(id: Long): F[Option[ConferenceMapping]]

  def findConferenceMappingBySeason(id: Long) :F[List[ConferenceMapping]]
}
