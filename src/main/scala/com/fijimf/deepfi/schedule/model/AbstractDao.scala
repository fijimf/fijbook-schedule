package com.fijimf.deepfi.schedule.model

import doobie.implicits._
import doobie.util.fragment.Fragment
trait AbstractDao {
  def cols: Array[String]
  def tableName:String

  def colString: String = cols.mkString(", ")

  def colFr: Fragment = Fragment.const(colString)
  def baseQuery: Fragment = fr"""SELECT """ ++ Fragment.const(colString) ++ fr""" FROM """++ Fragment.const(tableName+" ")

  def prefixedCols(p:String): Array[String] = cols.map(s=>p+"."+s)
  def prefixedQuery(p:String): Fragment = fr"""SELECT """ ++ Fragment.const(prefixedCols(p).mkString(",")) ++ fr""" FROM """++ Fragment.const(tableName+" ")

}
