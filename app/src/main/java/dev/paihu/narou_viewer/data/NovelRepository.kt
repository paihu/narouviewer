package dev.paihu.narou_viewer.data

import java.time.ZonedDateTime

class NovelRepository {

    fun novelPagingSource() = NovelPagingSource()

    fun find(id: Int): Novel {
        return Novel(
            "Title $id",
            "Author $id",
            "narou",
            "narou",
            updatedAt = ZonedDateTime.now(),
            createdAt = ZonedDateTime.now()
        )
    }
}