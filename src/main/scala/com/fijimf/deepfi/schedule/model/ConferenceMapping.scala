package com.fijimf.deepfi.schedule.model

import doobie.implicits._
import doobie.util.fragment.Fragment
import doobie.util.update.Update0

final case class ConferenceMapping(id: Long, seasonId: Long, teamId: Long, conferenceId: Long) {

}

object ConferenceMapping {

  object Dao extends AbstractDao {
    val cols: Array[String] = Array("id", "season_id", "team_id", "conference_id")
    val tableName="conference_mapping"

    def insert(cm: ConferenceMapping): Update0 =
      sql"""
    INSERT INTO conference_mapping(season_id, team_id, conference_id)
    VALUES (${cm.seasonId}, ${cm.teamId}, ${cm.conferenceId})
    RETURNING $colString """.update


    def find(id: Long): doobie.Query0[ConferenceMapping] = (baseQuery ++
      fr"""
       WHERE id = $id
      """).query[ConferenceMapping]

    def list(): doobie.Query0[ConferenceMapping] = baseQuery.query[ConferenceMapping]


    def delete(id: Long): doobie.Update0 =
      sql"""
        DELETE FROM conference_mapping where id=${id}
      """.update

    def update(cm: ConferenceMapping): Update0 =
      sql"""
        UPDATE conference_mapping SET season_id = ${cm.seasonId}, team_id = ${cm.teamId}, conference_id=${cm.conferenceId}
        WHERE id=${cm.id}
        """.update
  }
}

