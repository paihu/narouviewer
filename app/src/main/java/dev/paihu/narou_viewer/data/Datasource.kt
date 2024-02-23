package dev.paihu.narou_viewer.data

import java.time.ZonedDateTime

object Datasource {
    fun loadNovels(): List<Novel> {
        return (1..100).map {
            Novel(
                "$it",
                "novel$it",
                "author$it",
                "narou",
                0,
                ZonedDateTime.now(),
                ZonedDateTime.now()
            )
        }
    }

    fun loadPages(novelId: String, novelType: String): List<Page> {
        return (1..100).map {
            Page(
                "$it",
                it,
                novelId,
                novelType,
                "page$novelId-$it",
                "pagecontent",
                ZonedDateTime.now(),
                ZonedDateTime.now()
            )
        }
    }

    fun loadPage(pageNum: Int): Page {
        return Page(
            "$pageNum",
            1,
            "novelId",
            "pageId",
            "hoge",
            "content",
            ZonedDateTime.now(),
            ZonedDateTime.now()
        )
    }
}