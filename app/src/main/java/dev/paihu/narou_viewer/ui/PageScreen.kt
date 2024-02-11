package dev.paihu.narou_viewer.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import dev.paihu.narou_viewer.ITEMS_PER_PAGE
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.Datasource
import dev.paihu.narou_viewer.data.PageRepository
import dev.paihu.narou_viewer.model.Page
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme
import kotlinx.coroutines.flow.flowOf
import java.time.ZonedDateTime

@Composable
fun PageScreen(novelId: Int, click: (id: Int) -> Unit) {
    val pageFlow = remember {
        Pager(
            config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
            pagingSourceFactory = { PageRepository().pagePagingSource(novelId) }
        ).flow
    }
    val pages = pageFlow.collectAsLazyPagingItems()
    Pages(pages, click)
}

@Composable
fun Pages(pages: LazyPagingItems<Page>, click: (id: Int) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn {
        items(pages.itemCount) { index ->
            val page = pages[index]!!
            PageCard(page, { click(page.id) })
        }
    }
}

@Composable
@Preview
fun PagePreview() {
    NarouviewerTheme {
        Pages(
            flowOf(PagingData.from(Datasource.loadPages(1))).collectAsLazyPagingItems(),
            { id -> Log.w("pages", "$id")})
    }
}

@Composable
fun PageCard(page: Page, click: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small))
                .clickable { click() }
        ) {
            Text(
                text = page.title,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
@Preview
private fun PageCardPreview() {
    NarouviewerTheme {
        PageCard(Page(1,1, 1, "Page1", "content", "nakami", ZonedDateTime.now()), {})
    }
}