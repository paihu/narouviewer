package dev.paihu.narou_viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.paihu.narou_viewer.data.NovelRepository
import dev.paihu.narou_viewer.data.NovelState
import dev.paihu.narou_viewer.model.Novel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val ITEMS_PER_PAGE = 30

class NovelViewModel(novelRepository: NovelRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(NovelState())
    val uiState = _uiState.asStateFlow()

    val novels: Flow<PagingData<Novel>> = Pager(
        config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
        pagingSourceFactory = { novelRepository.novelPagingSource() }
    ).flow.cachedIn(viewModelScope)

    val currentNovel = novelRepository.find(this.uiState.value.selectedNovel)

    fun selectNovel(id: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedNovel = id,

                )
        }
    }

    fun selectPage(id: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedPage = id,
            )
        }

    }
}