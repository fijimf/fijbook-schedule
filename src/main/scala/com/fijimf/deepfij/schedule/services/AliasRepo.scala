package com.fijimf.deepfij.schedule.services
import cats.MonadError
import com.fijimf.deepfij.schedule.model.Alias

trait AliasRepo[F[_]] {

  def insertAlias(a: Alias)(implicit me: MonadError[F, Throwable]): F[Alias]

  def updateAlias(c: Alias): F[Alias]

  def deleteAlias(id: Long): F[Int]

  def listAliases(): F[List[Alias]]

  def findAlias(id: Long): F[Option[Alias]]
}
