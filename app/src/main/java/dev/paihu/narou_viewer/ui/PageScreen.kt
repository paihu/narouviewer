package dev.paihu.narou_viewer.ui

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
import dev.paihu.narou_viewer.model.Page
import dev.paihu.narou_viewer.ui.theme.NarouviewerTheme
import java.time.LocalDateTime

@Composable
fun PageScreen(pages: List<Page>, click: (id: Int) -> Unit) {
    Pages(pages, click)
}


@Composable
fun Pages(pages: List<Page>, click: (id: Int) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn {
        items(pages) { page ->
            PageCard(page, { click(page.id) })
        }
    }
}

@Composable
@Preview
fun PagePreview() {
    NarouviewerTheme {
        Pages(Datasource.loadPages(1), { id -> })
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
        PageCard(Page(1, 1, 1, "Page1", "content", LocalDateTime.now()), {})
    }
}