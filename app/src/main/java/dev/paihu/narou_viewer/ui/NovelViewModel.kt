package dev.paihu.narou_viewer.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


data class NovelState(val page: String = "novels", val novel: String? = null)
class NovelViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NovelState())
    val uiState = _uiState.asStateFlow()

    val page: String
        get() = uiState.value.page
}