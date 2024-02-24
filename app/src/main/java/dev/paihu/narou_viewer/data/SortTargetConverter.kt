package dev.paihu.narou_viewer.data

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@ProvidedTypeConverter
class ZonedDateTimeConverter {

    @TypeConverter
    fun fromZonedDateTime(value: ZonedDateTime?): Long? {
        return value?.toEpochSecond()
    }

    @TypeConverter
    fun toZonedDateTime(value: Long?): ZonedDateTime? {
        return value?.let {
            ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(it),
                ZoneId.systemDefault()
            )
        }
    }
}

enum class SortTarget(val column: String) {
    CreatedAt("created_at"),
    UpdatedAt("updated_at"),
}


class SortTargetConverter {
    @TypeConverter
    fun fromSortTarget(value: SortTarget) = value.column
}
