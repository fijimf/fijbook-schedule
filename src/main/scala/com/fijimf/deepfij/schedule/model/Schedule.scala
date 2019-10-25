package com.fijimf.deepfij.schedule.model

case class Schedule(season:Season, teams:List[Team], conferences:List[Conference], conferenceMapping:List[ConferenceMapping], games:List[Game], results:List[Result]) {


}
