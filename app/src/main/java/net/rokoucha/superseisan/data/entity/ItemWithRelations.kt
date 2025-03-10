package net.rokoucha.superseisan.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


data class ItemWithRelations(
    @Embedded val item: Item,

    @Relation(
        parentColumn = "currency_id",
        entityColumn = "id"
    )
    val currency: Currency?,

    @Relation(
        parentColumn = "payer_id",
        entityColumn = "id"
    )
    val payer: Participant?,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            Benefited::class,
            parentColumn = "item_id",
            entityColumn = "participant_id"
        )
    )
    val benefited: List<Participant>
)
