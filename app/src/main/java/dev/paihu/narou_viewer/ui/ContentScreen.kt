package dev.paihu.narou_viewer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import dev.paihu.narou_viewer.ITEMS_PER_PAGE
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.PageRepository

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentScreen(novelId: Int, initialPage: Int) {
    val pageFlow = remember {
        Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            pagingSourceFactory = { PageRepository().pagePagingSource(novelId) }
        ).flow
    }
    val pages = pageFlow.collectAsLazyPagingItems()
    val pageState = rememberPagerState(pageCount = { pages.itemCount }, initialPage = initialPage)
    HorizontalPager(
        state = pageState,
    ) { index ->
        val page = pages[index]!!
        Card(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(id = R.dimen.padding_small))
            ) {
                Text(page.title)
                Text(page.pageText)
            }

        }

    }

}