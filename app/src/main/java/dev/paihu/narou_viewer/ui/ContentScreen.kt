package dev.paihu.narou_viewer.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.AppDatabase
import java.time.ZonedDateTime

@Composable
fun ContentScreen(db: AppDatabase, novelId: String, novelType: String, initialPage: Int) {
    val pageFlow = remember { db.pageDao().getAllFlow(novelId, novelType) }
    val pages by pageFlow.collectAsState(initial = emptyList())
    val count = pages.size

    HorizontalPager(
        state = rememberPagerState(
            pageCount = { count },
            initialPage = initialPage
        )
    ) { index ->

        val scrollState = rememberScrollState()

        val page = pages[index]
        if (!scrollState.canScrollForward) LaunchedEffect(db) {
            db.pageDao().upsert(page.copy(readAt = ZonedDateTime.now()))
        }
        Card(
            modifier = Modifier
                .fillMaxSize()
        ) {
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