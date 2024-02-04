package dev.paihu.narou_viewer.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.paging.compose.collectAsLazyPagingItems
import dev.paihu.narou_viewer.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentScreen(viewModel: ContentViewModel) {
    val pages = viewModel.pages.collectAsLazyPagingItems()
    val pageState = rememberPagerState(pageCount = { pages.itemCount })
    HorizontalPager(
        state = pageState,
    ) { index ->
        val page = pages[index]!!
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