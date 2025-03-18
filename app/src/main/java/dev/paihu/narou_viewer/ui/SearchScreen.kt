package dev.paihu.narou_viewer.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dev.paihu.narou_viewer.ITEMS_PER_PAGE
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.backgroud.Downloader
import dev.paihu.narou_viewer.data.Novel
import dev.paihu.narou_viewer.network.KakuyomuPagingSource
import dev.paihu.narou_viewer.network.NarouSearchPagingSource
import dev.paihu.narou_viewer.network.Narou18SearchPagingSource
@Composable
fun SearchScreen(onBack: () -> Unit) {
    BackHandler {
        onBack()
    }
    Search()
}


@Composable
fun Search() {
    var searchWord by rememberSaveable { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable {
        mutableStateOf("narou")
    }

    val click = { query = searchWord }
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
            Row(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))) {
                TextButton(onClick = { click() }) {
                    Text("検索")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("なろう")
                    RadioButton(selected = type == "narou", onClick = { type = "narou" })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("カクヨム")
                    RadioButton(selected = type == "kakuyomu", onClick = { type = "kakuyomu" })

                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("なろう18")
                    RadioButton(selected = type == "narou18", onClick = { type = "narou18" })
                }

            }

        }
        if (!query.isEmpty()) {
            SearchResult(query = query, type = type) {
                openAlertDialog.value = it
            }
        }

    }
    openAlertDialog.value?.let {
        DownloadDialog(novel = it, close = { openAlertDialog.value = null })
    }

}

@Composable
fun SearchResult(query: String, type: String, click: (novel: Novel) -> Unit) {
    val novelFlow =
        Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            pagingSourceFactory = {
                when (type) {
                    "kakuyomu" -> KakuyomuPagingSource(query)
                    "narou18"->Narou18SearchPagingSource(query)
                    else -> NarouSearchPagingSource(query)
                }
            }
        ).flow

    val novels = novelFlow.collectAsLazyPagingItems()
    LazyColumn(
        Modifier
            .fillMaxSize()
    ) {
        items(novels.itemCount) {
            val novel = novels[it] ?: return@items
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                .padding(bottom = dimensionResource(id = R.dimen.padding_small))
                .clickable { click(novel) }
            ) {
                Column {
                    Text(novel.title)
                    Text(novel.author)
                }
            }
        }
    }

}

@Composable
fun DownloadDialog(novel: Novel, close: () -> Unit) {
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
                                    "type" to novel.type,
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