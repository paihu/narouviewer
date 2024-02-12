package dev.paihu.narou_viewer.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import java.time.ZonedDateTime

@Entity(
    tableName = "novels",
    primaryKeys = ["type", "novel_id"],
    indices = [Index(value = ["created_at"], name = "created")],
)
@TypeConverters(ZonedDateTimeConverter::class)
data class Novel(
    @ColumnInfo(name = "novel_id") val novelId: String,
    val author: String,
    val type: String,
    val title: String,
    @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
    @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,

    )