package dev.paihu.narou_viewer.data

import dev.paihu.narou_viewer.model.Novel
import dev.paihu.narou_viewer.model.Page
import java.time.ZonedDateTime

object Datasource {
    fun loadNovels(): List<Novel> {
        return (1..100).map {
            Novel(it, "novel$it", "author$it", null, "narou")
        }
    }

    fun loadPages(novelId: Int): List<Page> {
        return (1..100).map {
            Page(it, novelId,  "page$novelId-$it", "pagecontent", "nakami", ZonedDateTime.now())
        }
    }

    fun loadPage(pageId: Int): Page {
        return Page(1,  pageId,"pageId", "hoge", "content",  ZonedDateTime.now())
    }
}