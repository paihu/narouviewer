package dev.paihu.narou_viewer.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.TypeConverters
import java.time.ZonedDateTime

@Entity(
    tableName = "pages",
    foreignKeys = [ForeignKey(
        parentColumns = arrayOf("type", "novel_id"),
        childColumns = arrayOf("novel_type", "novel_id"),
        onDelete = ForeignKey.CASCADE,
        entity = Novel::class
    )],
    primaryKeys = ["novel_type", "novel_id", "page_id"],
    indices = [Index(value = ["novel_type", "novel_id", "num"], unique = true)]
)
@TypeConverters(ZonedDateTimeConverter::class)
data class Page(
    @ColumnInfo(name = "page_id") val pageId: String,
    @ColumnInfo(name = "num") val num: Int,
    @ColumnInfo(name = "novel_id") val novelId: String,
    @ColumnInfo(name = "novel_type") val novelType: String,
    val title: String,
    val content: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
    @ColumnInfo(name = "downloaded_at") val downloadedAt: ZonedDateTime? = null,
    @ColumnInfo(name = "read_at") val readAt: ZonedDateTime? = null,
)
