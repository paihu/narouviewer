package dev.paihu.narou_viewer.data

import android.content.Context
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

object DatabaseBackupHelper {
    private const val TAG = "DatabaseBackupHelper"
    private const val DB_NAME = "app.db"

    fun backupDatabase(context: Context, db: AppDatabase, outputStream: OutputStream): Boolean {
        return try {
            // Checkpoint to ensure all data is in the main .db file (not in WAL)
            db.openHelper.writableDatabase.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)"))

            val dbFile = context.getDatabasePath(DB_NAME)
            if (dbFile.exists()) {
                FileInputStream(dbFile).use { input ->
                    input.copyTo(outputStream)
                }
                true
            } else {
                Log.e(TAG, "Database file does not exist")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed", e)
            false
        }
    }

    fun restoreDatabase(context: Context, inputStream: InputStream): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val dbDir = dbFile.parentFile

            // Delete existing database files including WAL/SHM
            if (dbFile.exists()) {
                context.deleteDatabase(DB_NAME)
            }

            if (dbDir != null && !dbDir.exists()) {
                dbDir.mkdirs()
            }

            FileOutputStream(dbFile).use { output ->
                inputStream.copyTo(output)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed", e)
            false
        }
    }
}
