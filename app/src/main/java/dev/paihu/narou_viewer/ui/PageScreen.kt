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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import dev.paihu.narou_viewer.ITEMS_PER_PAGE
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.AppDatabase
import dev.paihu.narou_viewer.data.Datasource
import dev.paihu.narou_viewer.data.Page
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@Composable
fun PageScreen(
    db: AppDatabase,
    novelId: String,
    novelType: String,
    click: (num: Int) -> Unit
) {
    val pageFlow = remember {
        Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            pagingSourceFactory = {
                db.pageDao().getPagingSource(novelId, novelType)
            }
        ).flow
    }
    val pages = pageFlow.collectAsLazyPagingItems()
    val countFlow = db.pageDao().count(novelId,novelType)
    val count by countFlow.collectAsState(initial = 0)
    val longClick: (id: Int) -> Unit = { id ->
        pages[id]?.let {
            CoroutineScope(Dispatchers.IO).launch { db.pageDao().upsert(it.copy(readAt = null)) }
        }
    }
    Pages(pages, count, longClick = longClick, click = click)
}

@Composable
fun Pages(
    pages: LazyPagingItems<Page>,
    count: Int,
    longClick: (id: Int) -> Unit,
    click: (id: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
    ) {
        items(pages.itemCount, pages.itemKey()) { index ->
            val page = pages[index] ?: return@items
            PageCard(
                page,
                count,
                { if (page.readAt != null) longClick(index) },
                { click(page.num) })
        }
    }
}

@Composable
@Preview
fun PagePreview() {
    NarouviewerTheme {
        Pages(
            flowOf(PagingData.from(Datasource.loadPages("1", "narou"))).collectAsLazyPagingItems(),
            100,
            { id -> Log.w("pages", "LongClick $id") },
            { id -> Log.w("pages", "$id")})
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