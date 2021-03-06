package com.fijimf.deepfij.schedule.services
import cats.MonadError
import com.fijimf.deepfij.schedule.model.Alias

trait AliasRepo[F[_]] {
  implicit val me: MonadError[F, Throwable]

  def insertAlias(a: Alias): F[Alias]

  def updateAlias(c: Alias): F[Alias]

  def deleteAlias(id: Long): F[Int]

  def listAliases(): F[List[Alias]]

  def findAlias(id: Long): F[Option[Alias]]
}
