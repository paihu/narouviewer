package dev.paihu.narou_viewer.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


enum class SortTarget(s: String) {
    createdAt("created_at"),
    updatedAt("updated_at"),
}

class Converters {

    @TypeConverter
    fun toSortTarget(value: String) = enumValueOf<SortTarget>(value)

    @TypeConverter
    fun fromSortTarget(value: SortTarget) = value.name
}

@Dao
interface NovelDao {

    @Query("Select * from novels order by :order asc")
    fun getAll(order: SortTarget = SortTarget.createdAt): PagingSource<Int, Novel>

    @Query("select  * from novels where id = :novelId and type = :type limit 1")
    fun select(novelId: String, type: String): Flow<Novel>

    @Upsert
    suspend fun upsert(vararg novels: Novel)

    @Delete
    suspend fun delete(vararg novels: Novel)
}