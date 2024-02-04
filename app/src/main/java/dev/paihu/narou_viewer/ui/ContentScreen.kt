package dev.paihu.narou_viewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.paihu.narou_viewer.model.Page

@Composable
fun ContentScreen(page: Page) {
    Column {
        Text(page.title)
        Text(page.pageText)
    }
}