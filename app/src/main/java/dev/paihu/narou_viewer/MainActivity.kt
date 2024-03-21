package dev.paihu.narou_viewer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dev.paihu.narou_viewer.data.initDb
import dev.paihu.narou_viewer.datastore.appStateDataStore
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme

class MainActivity : ComponentActivity() {
    private val db by lazy { initDb(applicationContext) }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val uri = Uri.parse(intent?.extras?.getString("android.intent.extra.TEXT") ?: "")
        setResult(Activity.RESULT_OK)

        setContent {
            NarouviewerTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NovelApp(db, applicationContext.appStateDataStore, uri)
                }
            }
        }
    }

}


