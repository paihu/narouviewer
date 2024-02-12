package dev.paihu.narou_viewer.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(
    tableName = "novels",
    indices = [Index(value = ["created_at"], name = "created"), Index(
        value = ["type", "novel_id"],
        unique = true
    )],
)
data class Novel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "novel_id") val novelId: String,
    val type: String,
    val title: String,
    @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
    @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,

    )