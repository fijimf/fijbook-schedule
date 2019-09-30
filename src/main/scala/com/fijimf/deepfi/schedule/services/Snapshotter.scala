package com.fijimf.deepfi.schedule.services

class Snapshotter[F[_]] {
  def writeStaticData(bucket:String, key:String):F[Unit] = ??? /* Alias Team Conference */
  def readStaticData(bucket:String, key:String):F[Unit] = ???
  def writeSeasonalData(bucket:String, key:String):F[Unit] = ??? /* Season ConferenceMapping Game Result */
  def readSeasonalData(bucket:String, key:String):F[Unit] = ???
}
