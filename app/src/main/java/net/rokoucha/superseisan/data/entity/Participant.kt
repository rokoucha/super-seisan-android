package net.rokoucha.superseisan.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "participants",
    indices = [
        Index(
            value = ["settlement_id"]
        ),
        Index(
            value = ["settlement_id", "name"],
            unique = true
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = Settlement::class,
            parentColumns = ["id"],
            childColumns = ["settlement_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Participant(
    @PrimaryKey(autoGenerate = false)
    val id: String,

    val name: String,

    @ColumnInfo(name = "settlement_id")
    val settlementId: String
)
