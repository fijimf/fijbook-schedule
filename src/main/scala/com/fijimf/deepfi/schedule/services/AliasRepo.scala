package com.fijimf.deepfi.schedule.services
import com.fijimf.deepfi.schedule.model.Alias

trait AliasRepo[F[_]] {

  def insertAlias(a: Alias): F[Alias]

  def updateAlias(c: Alias): F[Alias]

  def deleteAlias(id: Long): F[Int]

  def listAliases(): F[List[Alias]]

  def findAlias(id: Long): F[Option[Alias]]
}
