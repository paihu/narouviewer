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