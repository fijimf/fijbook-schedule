package com.fijimf.deepfij.schedule.services
import com.fijimf.deepfij.schedule.model.Result

trait ResultRepo[F[_]] {

  def insertResult(r: Result): F[Result]

  def updateResult(r: Result): F[Result]

  def deleteResult(id: Long): F[Int]

  def listResult(): F[List[Result]]

  def findResult(id: Long): F[Option[Result]]
}
