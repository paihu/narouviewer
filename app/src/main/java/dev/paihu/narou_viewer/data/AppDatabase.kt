package dev.paihu.narou_viewer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Novel::class, Page::class], version = 3, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pageDao(): PageDao
    abstract fun novelDao(): NovelDao

}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("alter table novels add last_read_page integer default 0;")

    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP INDEX `index_pages_novel_type_novel_id_num`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pages_novel_type_novel_id_num` ON `pages` (`novel_type`, `novel_id`, `num`)")

    }
}

fun initDb(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java, "app.db"
    ).addTypeConverter(ZonedDateTimeConverter()).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        .build()
}