package dev.paihu.narou_viewer.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.backgroud.Downloader
import dev.paihu.narou_viewer.model.Novel
import dev.paihu.narou_viewer.network.NarouService
import kotlinx.coroutines.launch

@Composable
fun SearchScreen() {
    NarouSearch()
}


@Composable
fun NarouSearch() {
    val state = remember { mutableStateListOf<Novel>() }
    var searchWord by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val click = {
        scope.launch {
            state.clear()
            state.addAll(NarouService.search(searchWord))
        }
    }
    val openAlertDialog = remember { mutableStateOf<Novel?>(null) }
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small))
        ) {
            Text(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small)), text = "タイトルサーチ"
            )
            OutlinedTextField(
                value = searchWord,
                onValueChange = { searchWord = it }
            )
            TextButton(onClick = { click() }) {
                Text("検索")
            }

        }
        LazyColumn(
            Modifier
                .fillMaxSize()
        ) {
            items(state) {
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_small))
                    .clickable { openAlertDialog.value = it }
                ) {
                    Text(it.title)
                }
            }
        }
    }
    openAlertDialog.value?.let {
        DowloadDialog(novel = openAlertDialog.value!!, close = { openAlertDialog.value = null })
    }

}

@Composable
fun DowloadDialog(novel: Novel, close: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(onDismissRequest = close,
        text = {
            Card {
                Text(novel.title)
                Text(novel.author)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "narou",
                        ExistingWorkPolicy.APPEND_OR_REPLACE,
                        OneTimeWorkRequestBuilder<Downloader>()
                            .addTag("narou")
                            .setInputData(
                                workDataOf(
                                    "type" to "narou",
                                    "mode" to "novel",
                                    "novelId" to novel.novelId,
                                )
                            ).build()
                    )
                    Toast.makeText(context, "download開始しました", Toast.LENGTH_LONG).show()
                    close()
                }
            ) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    close()
                }
            ) {
                Text("Dismiss")
            }
        }
    )

}