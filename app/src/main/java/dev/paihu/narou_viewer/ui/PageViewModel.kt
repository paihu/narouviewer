package dev.paihu.narou_viewer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.paihu.narou_viewer.data.PageRepository
import dev.paihu.narou_viewer.model.Page
import kotlinx.coroutines.flow.Flow

private val ITEMS_PER_PAGE = 30

class PageViewModel(novelId: Int, pageRepository: PageRepository) :
    ViewModel() {
    val pages: Flow<PagingData<Page>> = Pager(
        config = PagingConfig(pageSize = ITEMS_PER_PAGE, enablePlaceholders = false),
        pagingSourceFactory = { pageRepository.pagePagingSource(novelId) }
    ).flow.cachedIn(viewModelScope)


}