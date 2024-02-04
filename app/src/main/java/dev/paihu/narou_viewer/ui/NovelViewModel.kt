package dev.paihu.narou_viewer.ui

import androidx.lifecycle.ViewModel
import dev.paihu.narou_viewer.data.Datasource
import dev.paihu.narou_viewer.data.NovelState
import dev.paihu.narou_viewer.model.Novel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class NovelViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NovelState())
    val uiState = _uiState.asStateFlow()

    val novels: List<Novel>
        get() = uiState.value.novels

    fun loadNovels() {
        _uiState.update { currentState -> currentState.copy(novels = Datasource.loadNovels()) }
    }

    fun selectNovel(id: Int) {
        _uiState.update { currentState -> currentState.copy(selectedNovel = id) }
    }

    fun selectPage(id: Int) {
        _uiState.update { currentState -> currentState.copy(selectedPage = id) }

    }
}