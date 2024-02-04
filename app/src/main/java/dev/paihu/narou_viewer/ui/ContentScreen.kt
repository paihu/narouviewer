package dev.paihu.narou_viewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.model.Page

@Composable
fun ContentScreen(page: Page) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(id = R.dimen.padding_small))
    ) {
        Text(page.title)
        Text(page.pageText)
    }
}