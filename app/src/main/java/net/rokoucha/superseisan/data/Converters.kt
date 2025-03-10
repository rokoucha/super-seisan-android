package net.rokoucha.superseisan.data

import androidx.room.TypeConverter
import java.time.OffsetDateTime

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): OffsetDateTime? {
        return value?.let { OffsetDateTime.parse(it) }
    }

    @TypeConverter
    fun toTimestamp(date: OffsetDateTime?): String? {
        return date?.toString()
    }
}
