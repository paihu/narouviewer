package dev.paihu.narou_viewer.ui

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.AppDatabase
import dev.paihu.narou_viewer.data.Datasource
import dev.paihu.narou_viewer.data.Novel
import dev.paihu.narou_viewer.data.Page
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@Composable
fun PageScreen(
    db: AppDatabase,
    novel: Novel,
    click: (num: Int) -> Unit
) {
    val pageFlow = remember(key1 = novel) {
        db.pageDao().getAllFlow(novel.novelId, novel.type)
    }

    val pages by pageFlow.collectAsState(initial = emptyList())
    val longClick: (id: Int) -> Unit = { id ->
        val page = pages[id]
        if (page.readAt != null) CoroutineScope(Dispatchers.IO).launch {
            db.pageDao().upsert(page.copy(readAt = null))
        }
    }
    Pages(pages, novel.lastReadPage ?: 0, longClick = longClick, click = click)
}

@Composable
fun Pages(
    pages: List<Page>,
    initialPageNum: Int,
    longClick: (id: Int) -> Unit,
    click: (id: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyListState()
    LazyColumn(state = state) {
        items(pages.size) { index ->
            val page = pages[index]
            PageCard(
                page,
                pages.size,
                { longClick(index) },
                { click(page.num) })
        }
    }
    LaunchedEffect(Unit) {
        state.scrollToItem(maxOf(0, initialPageNum - 5))

    }
}

@Composable
@Preview
fun PagesPreview() {
    NarouviewerTheme {
        Pages(
            Datasource.loadPages("1", "narou"),
            0,
            { id -> Log.w("pages", "LongClick $id") },
            { id -> Log.w("pages", "$id") })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PageCard(
    page: Page,
    totalPage: Int,
    longClick: () -> Unit,
    click: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small))
                .combinedClickable(
                    onLongClick = longClick
                ) { click() }
        ) {
            Row {
                if (page.readAt != null) {
                    if (page.readAt < page.downloadedAt) {
                        Text("(新)")
                    } else {
                        Text("(読)")
                    }
                } else {
                    Text("(未)")
                }
                Text(
                    text = "${page.num}/${totalPage} ${page.title}",
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Composable
@Preview
private fun PageCardPreview() {
    NarouviewerTheme {
        PageCard(
            Page(
                "1",
                1,
                "novelId1",
                "narou",
                "Page1",
                "content",
                ZonedDateTime.now(),
                ZonedDateTime.now()
            ), 10, {}, {})
    }
}