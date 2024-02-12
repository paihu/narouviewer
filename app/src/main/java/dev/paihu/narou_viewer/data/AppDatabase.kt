package dev.paihu.narou_viewer.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Novel::class, Page::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pageDao(): PageDao
    abstract fun novelDao(): NovelDao

}