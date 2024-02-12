package dev.paihu.narou_viewer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import dev.paihu.narou_viewer.ITEMS_PER_PAGE
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import kotlin.math.min

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentScreen(db: AppDatabase, novelId: String, novelType: String, initialPage: Int) {
    val countFlow = db.pageDao().count(novelId,novelType)
    val count by countFlow.collectAsState(initial = 0)
    if(count==0)return
    val pageFlow = remember {
        Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            initialKey =  min(count-90,initialPage-45),
            pagingSourceFactory = {
                db.pageDao().getPagingSource(novelId, novelType)
            }
        ).flow
    }
    val pages = pageFlow.collectAsLazyPagingItems()
    val pageState = rememberPagerState(pageCount = { pages.itemCount }, initialPage = maxOf(90-(count-initialPage), 45))

    HorizontalPager(
        state = pageState,
    ) { index ->
        val scrollState = rememberScrollState()

        val page = pages[index]!!
        if(!scrollState.canScrollForward){
            CoroutineScope(Dispatchers.IO).launch{
                db.pageDao().upsert(page.copy(readAt= ZonedDateTime.now()))
            }
        }
        Card(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text("(${page.num}/${count}) ${page.title}")
                Text(page.content ?: "not downloaded")
            }

        }

    }


}