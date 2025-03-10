package net.rokoucha.superseisan.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SettlementWithParticipants(
    @Embedded
    val settlement: Settlement,

    @Relation(
        parentColumn = "id",
        entityColumn = "settlement_id"
    )
    val participants: List<Participant>
)
