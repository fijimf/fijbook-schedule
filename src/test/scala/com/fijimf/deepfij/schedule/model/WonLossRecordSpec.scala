package com.fijimf.deepfij.schedule.model

import org.scalatest.FunSpec

class WonLossRecordSpec extends FunSpec {
  describe("A list of WonLossRecords") {
    it("can be sorted") {
      val records = List(WonLossRecord(5, 4), WonLossRecord(6, 7), WonLossRecord(7, 6), WonLossRecord(10, 2), WonLossRecord(7, 7))
      val sorted = List(WonLossRecord(10, 2), WonLossRecord(7, 6), WonLossRecord(5, 4), WonLossRecord(7, 7), WonLossRecord(6, 7))
      assert(records.sorted === sorted)
    }
  }

}
