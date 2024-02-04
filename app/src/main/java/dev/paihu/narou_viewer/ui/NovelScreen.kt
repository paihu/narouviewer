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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.Datasource
import dev.paihu.narou_viewer.model.Novel
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme
import kotlinx.coroutines.flow.flowOf

@Composable
fun NovelScreen(novels: LazyPagingItems<Novel>, click: (id: Int) -> Unit) {
    Novels(
        novels, click = click
    )
}


@Composable
fun Novels(
    novels: LazyPagingItems<Novel>,
    click: (id: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn {
        items(novels.itemCount) { index ->
            val novel = novels[index]
            if (novel != null) {
                NovelCard(novel, click = { click(novel.id) })
            }
        }
    }
}

@Composable
@Preview
fun NovelsPreview() {
    NarouviewerTheme {
        Novels(
            flowOf(PagingData.from(Datasource.loadNovels())).collectAsLazyPagingItems(),
            { id -> Log.w("novels", "$id") })
    }
}

@Composable
fun NovelCard(novel: Novel, click: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small))
                .clickable { click() }
        ) {
            Text(text = novel.title, modifier = Modifier.padding(4.dp))
            Text(text = novel.author, modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
@Preview
private fun NovelCardPreview() {
    NarouviewerTheme {
        NovelCard(Novel(1, "Novel1", "Author1"), { Log.w("novel", "") })
    }
}