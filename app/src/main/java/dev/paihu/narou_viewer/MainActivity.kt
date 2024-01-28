package dev.paihu.narou_viewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.paihu.narou_viewer.data.Datasource
import dev.paihu.narou_viewer.model.Novel
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NarouviewerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Novels(Datasource().loadNovels())
                }
            }
        }
    }
}

@Composable
fun Novels(novels: List<Novel>, modifier: Modifier = Modifier) {
    LazyColumn {
        items(novels) { novel ->
            NovelCard(novel)
        }
    }
}

@Composable
@Preview
fun NovelsPreview() {
    NarouviewerTheme {
        Novels(Datasource().loadNovels())
    }
}

@Composable
fun NovelCard(novel: Novel, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_small))
        ) {
            Text(text = novel.title, modifier = Modifier.padding(4.dp))
        }
    }
}

@Composable
@Preview
private fun NovelCardPreview() {
    NovelCard(Novel(1,"Novel1"))
}