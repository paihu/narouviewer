package dev.paihu.narou_viewer.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
@TypeConverters(ZonedDateTimeConverter::class)
interface NovelDao {

    @Query("Select * from novels order by :order asc")
    @TypeConverters(SortTargetConverter::class)
    fun getAll(order: SortTarget = SortTarget.CreatedAt): List<Novel>

    @Query("Select * from novels order by :order asc")
    @TypeConverters(SortTargetConverter::class)
    fun getAllFlow(order: SortTarget = SortTarget.CreatedAt): Flow<List<Novel>>

    @Query("Select * from novels order by :order asc")
    @TypeConverters(SortTargetConverter::class)
    fun getPagingSource(order: SortTarget = SortTarget.CreatedAt): PagingSource<Int, Novel>


    @Query("select  * from novels where novel_id = :novelId and type = :type limit 1")
    fun select(novelId: String, type: String): Novel?

    @Upsert
    suspend fun upsert(vararg novels: Novel)

    @Delete
    suspend fun delete(vararg novels: Novel)
}