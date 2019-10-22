package com.fijimf.deepfij.schedule.services
import com.fijimf.deepfij.schedule.model.Game

trait GameRepo[F[_]] {

  def insertGame(g: Game): F[Game]

  def updateGame(g: Game): F[Game]

  def deleteGame(id: Long): F[Int]

  def listGame(): F[List[Game]]

  def findGame(id: Long): F[Option[Game]]
}
