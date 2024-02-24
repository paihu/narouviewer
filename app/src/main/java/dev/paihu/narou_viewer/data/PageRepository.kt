package dev.paihu.narou_viewer.data

class PageRepository {
    fun pagePagingSource(novelId: String) = PagePagingSource(novelId)

}