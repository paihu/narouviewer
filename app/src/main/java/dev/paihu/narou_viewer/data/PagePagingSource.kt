package dev.paihu.narou_viewer.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.paihu.narou_viewer.model.Page
import java.time.ZonedDateTime
import kotlin.math.max


class PagePagingSource(private val novelId: Int) :
    PagingSource<Int, Page>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Page> {
        val start = params.key ?: STARTING_KEY
        val range = start.until(start + params.loadSize)
        return LoadResult.Page(
            data = range.map { number ->
                Page(
                    // Generate consecutive increasing numbers as the article id
                    id = number,
                    novelId = novelId,
                    pageNum = number,
                    pageId = number.toString(),
                    title = "Article $novelId-$number",
                    pageText = "Author $number",
                    updateAt = ZonedDateTime.now(),
                )
            },

            // Make sure we don't try to load items behind the STARTING_KEY
            prevKey = when (start) {
                STARTING_KEY -> null
                else -> ensureValidKey(key = range.first - params.loadSize)
            },
            nextKey = range.last + 1
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Page>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val article = state.closestItemToPosition(anchorPosition) ?: return null
        return ensureValidKey(key = article.id - (state.config.pageSize / 2))
    }

    private fun ensureValidKey(key: Int) = max(STARTING_KEY, key)
}