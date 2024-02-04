package dev.paihu.narou_viewer.data

import dev.paihu.narou_viewer.model.Novel
import dev.paihu.narou_viewer.model.Page
import java.time.LocalDateTime

object Datasource {
    fun loadNovels(): List<Novel> {
        return (1..100).map {
            Novel(it, "novel$it", "author$it")
        }
    }

    fun loadPages(novelId: Int): List<Page> {
        return (1..100).map {
            Page(it, novelId, it, "page$novelId-$it", "pagecontent", LocalDateTime.now())
        }
    }

    fun loadPage(pageId: Int): Page {
        return Page(pageId, 1, 1, "hoge", "content", LocalDateTime.now())
    }
}