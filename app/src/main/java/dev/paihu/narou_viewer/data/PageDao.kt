package dev.paihu.narou_viewer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(ZonedDateTimeConverter::class)
interface PageDao {

    @Query("select * from pages where novel_type = :novelType and novel_id = :novelId  order by num")
    fun getAll(novelId: String, novelType: String): List<Page>

    @Query("select * from pages where novel_type = :novelType and novel_id = :novelId  order by num")
    fun getAllFlow(novelId: String, novelType: String): Flow<List<Page>>

    @Query("select * from pages where novel_type = :novelType and novel_id = :novelId and num=:pageNum limit 1")
    fun selectByNum(novelId: String, novelType: String, pageNum: Int): Page?

    @Query("select * from pages where novel_type = :novelType and novel_id = :novelId and page_id = :pageId limit 1")
    fun selectByPageId(novelId: String, novelType: String, pageId: String): Page?


    @Query("delete from pages where novel_type = :novelType and novel_id = :novelId and num = :pageNum")
    suspend fun deleteByNum(novelId: String, novelType: String, pageNum: Int)

    @Upsert
    suspend fun upsert(vararg pages: Page)

    @Delete
    suspend fun delete(vararg pages: Page)
}