package dev.paihu.narou_viewer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.AppDatabase
import dev.paihu.narou_viewer.data.Page
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

@Composable
fun ContentScreen(
    db: AppDatabase,
    novelId: String,
    novelType: String,
    initialPage: Int,
    onBack: () -> Unit
) {

    BackHandler {
        onBack()
    }
    val pageFlow = remember { db.pageDao().getAllFlow(novelId, novelType) }
    val pages by pageFlow.collectAsState(initial = emptyList())
    val count = pages.size
    val state = rememberPagerState(
        pageCount = { count },
        initialPage = initialPage
    )
    LaunchedEffect(state) {
        snapshotFlow { state.currentPage }.collect { pageNum ->
            CoroutineScope(Dispatchers.IO).launch {
                db.novelDao().select(novelId, novelType)?.let { novel ->
                    db.novelDao().upsert(novel.copy(lastReadPage = pageNum - 1))
                }
            }
        }
    }
    Contents(state = state, pages = pages, count = count) {
        val page = it
        CoroutineScope(Dispatchers.IO).launch {
            db.pageDao().upsert(page.copy(readAt = ZonedDateTime.now()))
        }
    }
}

@Composable
fun Contents(state: PagerState, pages: List<Page>, count: Int, read: (page: Page) -> Unit) {
    HorizontalPager(
        state = state
    ) { index ->

        val scrollState = rememberScrollState()

        val page = pages[index]
        if (!scrollState.canScrollForward) {
            read(page)
        }
        Card(
            modifier = Modifier
                .fillMaxSize()
        ) {
            SelectionContainer {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(id = R.dimen.padding_small))
                ) {
                    Text("(${page.num}/${count}) ${page.title}")
                    Text(
                        page.content ?: "not downloaded",
                        modifier = Modifier.verticalScroll(scrollState)
                    )
                }
            }
        }

    }


}