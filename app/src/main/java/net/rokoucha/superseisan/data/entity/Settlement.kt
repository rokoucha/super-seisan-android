package net.rokoucha.superseisan.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(
    tableName = "settlements",
    indices = [
        Index(value = ["last_accessed_at", "id"])
    ]
)
data class Settlement(
    @PrimaryKey(autoGenerate = false)
    val id: String,

    val name: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: OffsetDateTime,

    @ColumnInfo(name = "last_accessed_at")
    val lastAccessedAt: OffsetDateTime
)
