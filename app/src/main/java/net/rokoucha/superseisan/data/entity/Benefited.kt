package net.rokoucha.superseisan.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "benefited",
    primaryKeys = ["participant_id", "item_id"],
    indices = [
        androidx.room.Index("participant_id"),
        androidx.room.Index("item_id")
    ],
    foreignKeys = [
        ForeignKey(
            entity = Participant::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("participant_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Item::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("item_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Benefited(
    @ColumnInfo(name = "participant_id")
    val participantId: String,

    @ColumnInfo(name = "item_id")
    val itemId: String
)
