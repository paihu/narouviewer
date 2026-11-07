package dev.paihu.narou_viewer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.paihu.narou_viewer.data.initDb
import dev.paihu.narou_viewer.datastore.appStateDataStore
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme

class MainActivity : ComponentActivity() {
    private val db by lazy { initDb(applicationContext) }
    private var sharedUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedUri = extractUri(intent)
        setResult(Activity.RESULT_OK)

        setContent {
            NarouviewerTheme {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NovelApp(db, applicationContext.appStateDataStore, sharedUri)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        sharedUri = extractUri(intent)
    }

    private fun extractUri(intent: Intent?): Uri? {
        val text = intent?.extras?.getString("android.intent.extra.TEXT") ?: ""
        return if (text.isNotEmpty()) Uri.parse(text) else null
    }
}
