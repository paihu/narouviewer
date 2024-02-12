package dev.paihu.narou_viewer.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface PageDao {
    @Query("select * from pages where novel_id = :novelId order by num")
    fun getAll(novelId: String): PagingSource<Int, Page>

    @Query("select * from pages where novel_id = :novelId and num=:pageNum limit 1")
    fun select(novelId: String, pageNum: Int): Page


    @Upsert
    suspend fun upsert(vararg pages: Page)

    @Delete
    suspend fun delete(vararg pages: Page)
}