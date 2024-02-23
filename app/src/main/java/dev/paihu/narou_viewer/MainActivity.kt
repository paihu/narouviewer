package dev.paihu.narou_viewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import dev.paihu.narou_viewer.data.AppDatabase
import dev.paihu.narou_viewer.data.MIGRATION_1_2
import dev.paihu.narou_viewer.data.ZonedDateTimeConverter
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "app.db"
        ).addTypeConverter(ZonedDateTimeConverter()).addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries().build()
        setContent {
            NarouviewerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NovelApp(db)
                }
            }
        }
    }

}


