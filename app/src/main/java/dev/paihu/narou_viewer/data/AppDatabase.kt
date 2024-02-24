package dev.paihu.narou_viewer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Novel::class, Page::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pageDao(): PageDao
    abstract fun novelDao(): NovelDao

}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("alter table novels add last_read_page integer default 0;")

    }
}

fun initDb(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java, "app.db"
    ).addTypeConverter(ZonedDateTimeConverter()).addMigrations(MIGRATION_1_2)
        .build()
}