package dev.paihu.narou_viewer.data

import dev.paihu.narou_viewer.model.Novel

class NovelRepository {

    fun novelPagingSource() = NovelPagingSource()

    fun find(id: Int): Novel {
        return Novel(id, "Title $id", "Author $id", null, "narou")
    }
}