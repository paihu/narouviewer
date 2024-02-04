package dev.paihu.narou_viewer.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.Datasource
import dev.paihu.narou_viewer.model.Novel
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme

@Composable
fun NovelScreen(novels: List<Novel>, click: (id: Int) -> Unit) {
    Novels(
        novels, click = click
    )
}


@Composable
fun Novels(novels: List<Novel>, click: (id: Int) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn {
        items(novels) { novel ->
            NovelCard(novel, click = { click(novel.id) })
        }
    }
}

@Composable
@Preview
fun NovelsPreview() {
    NarouviewerTheme {
        Novels(Datasource.loadNovels(), { id -> Log.w("novels", "$id") })
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