package dev.paihu.narou_viewer.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.Datasource
import dev.paihu.narou_viewer.data.Novel
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme
import kotlinx.coroutines.flow.flowOf
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun NovelScreen(
    novels: LazyPagingItems<Novel>,
    click: (novel: Novel) -> Unit,
    download: (novel: Novel) -> Unit,
    delete: (novel: Novel) -> Unit
) {
    Novels(
        novels, click = click, download = download, delete = delete
    )
}

@Composable
fun Novels(
    novels: LazyPagingItems<Novel>,
    click: (novel: Novel) -> Unit,
    download: (novel: Novel) -> Unit,
    delete: (novel: Novel) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyColumn {
        items(novels.itemCount, novels.itemKey { "${it.type}-${it.novelId}" }) { index ->
            val novel = novels[index] ?: return@items
            NovelCard(
                novel,
                click = { click(novel) },
                download = { download(novel) },
                delete = {
                    delete(novel)
                }
            )
        }
    }

}

@Composable
@Preview
fun NovelsPreview() {
    NarouviewerTheme {
        Novels(
            flowOf(PagingData.from(Datasource.loadNovels())).collectAsLazyPagingItems(),
            { novel -> Log.w("novels", "click ${novel.novelId} ${novel.type}") },
            { novel -> Log.w("novels", "download ${novel.novelId} ${novel.type}") },
            { novel -> Log.w("novels", "delete ${novel.novelId} ${novel.type}") },
        )
    }
}

@Composable
fun NovelCard(
    novel: Novel,
    click: () -> Unit,
    download: () -> Unit,
    delete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var openDeleteDialog by remember { mutableStateOf(false) }
    when {
        openDeleteDialog -> {
            DeleteDialog(novel = novel, delete = delete) {
                openDeleteDialog = false
            }
        }
    }
    Card(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
                .weight(1f)
                .clickable { click() }
            ) {
                Text(text = novel.title, modifier = Modifier.padding(4.dp))
                Text(
                    text = "${novel.author} ${novel.type}:${novel.novelId}",
                )
                Text(
                    text =
                    novel.updatedAt.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Right
                )
            }
            Box(
                modifier = Modifier
                    .padding(end = dimensionResource(id = R.dimen.padding_medium))
                    .weight(0.1f)
            ) {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Localized description")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    NavigationDrawerItem(
                        onClick = {
                            expanded = false
                            openDeleteDialog = true
                        },
                        label = { Text("削除") },
                        selected = false,
                    )
                    NavigationDrawerItem(
                        onClick = {
                            expanded = false
                            download()
                        },
                        label = { Text("更新") },
                        selected = false,
                    )
                    NavigationDrawerItem(
                        onClick = {
                            expanded = false
                            val uri = when (novel.type) {
                                "narou" -> "https://ncode.syosetu.com/" + novel.novelId
                                else -> "https://kakuyomu.jp/works/" + novel.novelId
                            }
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            startActivity(context, intent, null)
                        },
                        label = { Text("小説ページ") },
                        selected = false,
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun NovelCardPreview() {
    NarouviewerTheme {
        NovelCard(
            Novel(
                title = "Novel1",
                author = "Author1",
                novelId = "hoge",
                type = "narou",
                createdAt = ZonedDateTime.now(),
                updatedAt = ZonedDateTime.now(),
            ),
            { Log.w("novel", "click") },
            { Log.w("novel", "download") },
            { Log.w("novel", "delete") },
        )
    }
}

@Composable
fun DeleteDialog(novel: Novel, delete: () -> Unit, close: () -> Unit) {
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
                    delete()
                    Toast.makeText(context, "削除しました", Toast.LENGTH_LONG).show()
                    close()
                }
            ) {
                Text("delete")
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