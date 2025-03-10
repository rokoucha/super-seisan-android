package net.rokoucha.superseisan.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    indices = [
        Index(
            value = ["settlement_id"]
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = Settlement::class,
            parentColumns = ["id"],
            childColumns = ["settlement_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Participant::class,
            parentColumns = ["id"],
            childColumns = ["payer_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Currency::class,
            parentColumns = ["id"],
            childColumns = ["currency_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = false)
    val id: String,

    val name: String,

    val price: Double,

    val quantity: Int,

    @ColumnInfo(name = "settlement_id")
    val settlementId: String,

    @ColumnInfo(name = "payer_id")
    val payerId: String?,

    @ColumnInfo(name = "currency_id")
    val currencyId: String?
)
