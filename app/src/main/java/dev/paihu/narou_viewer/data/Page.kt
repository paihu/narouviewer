package dev.paihu.narou_viewer.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(
    tableName = "pages",
    foreignKeys = [ForeignKey(
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("novel_id"),
        onDelete = ForeignKey.CASCADE,
        entity = Novel::class
    )],
    indices = [Index(value = ["novel_id", "num"], unique = true)]
)
data class Page(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "page_id") val pageId: String,
    @ColumnInfo(name = "num") val num: String,
    @ColumnInfo(name = "novel_id") val novelId: Int,
    val title: String,
    val content: String?,
    @ColumnInfo(name = "created_at") val createdAt: ZonedDateTime,
    @ColumnInfo(name = "updated_at") val updatedAt: ZonedDateTime,
    @ColumnInfo(name = "downloaded_at") val downloadedAt: ZonedDateTime?,
    @ColumnInfo(name = "read_at") val readAt: ZonedDateTime?,
)
