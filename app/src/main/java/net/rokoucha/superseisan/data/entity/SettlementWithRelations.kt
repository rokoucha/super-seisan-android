package net.rokoucha.superseisan.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class SettlementWithRelations(
    @Embedded val settlement: Settlement,

    @Relation(
        entity = Item::class,
        parentColumn = "id",
        entityColumn = "settlement_id"
    )
    val items: List<ItemWithRelations>,

    @Relation(
        parentColumn = "id",
        entityColumn = "settlement_id"
    )
    val participants: List<Participant>,

    @Relation(
        parentColumn = "id",
        entityColumn = "settlement_id"
    )
    val currencies: List<Currency>
)
