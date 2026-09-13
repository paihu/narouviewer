package dev.paihu.narou_viewer.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.paihu.narou_viewer.data.AppDatabase
import dev.paihu.narou_viewer.data.DatabaseBackupHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    db: AppDatabase,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRestoreDialog by remember { mutableStateOf(false) }
    var selectedRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let {
            CoroutineScope(Dispatchers.IO).launch {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val success = DatabaseBackupHelper.backupDatabase(context, db, outputStream)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(context, "バックアップ完了", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "バックアップ失敗", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedRestoreUri = it
            showRestoreDialog = true
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("データベースの復元") },
            text = { Text("現在のデータが全て上書きされます。よろしいですか？\n復元後、アプリを再起動してください。") },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreDialog = false
                    selectedRestoreUri?.let { uri ->
                        CoroutineScope(Dispatchers.IO).launch {
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                val success =
                                    DatabaseBackupHelper.restoreDatabase(context, inputStream)
                                withContext(Dispatchers.Main) {
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "復元完了。アプリを再起動してください。",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        Toast.makeText(context, "復元失敗", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }
                        }
                    }
                }) {
                    Text("復元する")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("キャンセル")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "データ管理",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                backupLauncher.launch("narou_viewer_backup.db")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("データベースをバックアップ")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                restoreLauncher.launch(arrayOf("application/octet-stream", "*/*"))
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("データベースを復元")
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("戻る")
        }
    }
}
