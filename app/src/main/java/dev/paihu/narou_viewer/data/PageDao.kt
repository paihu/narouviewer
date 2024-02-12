package dev.paihu.narou_viewer.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Upsert

@Dao
@TypeConverters(ZonedDateTimeConverter::class)
interface PageDao {
    @Query("select * from pages where novel_type = :novelType and novel_id = :novelId  order by num")
    fun getPagingSource(novelId: String, novelType: String): PagingSource<Int, Page>

    @Query("select * from pages where novel_type = :novelType and novel_id = :novelId  order by num")
    fun getAll(novelId: String, novelType: String): List<Page>

    @Query("select * from pages where novel_type = :novelType and novel_id = :novelId and num=:pageNum limit 1")
    fun select(novelId: String, novelType: String, pageNum: Int): Page?


    @Upsert
    suspend fun upsert(vararg pages: Page)

    @Delete
    suspend fun delete(vararg pages: Page)
}