package dev.paihu.narou_viewer.model

import java.time.ZonedDateTime

data class Page(
    val id:Int,
    val novelId: Int,
    val pageNum: Int,
    val pageId: String,
    val title: String,
    val pageText: String,
    val createdAt: ZonedDateTime? = null,
    val updateAt: ZonedDateTime? = null,
    val downloadAt: ZonedDateTime? = null,
    val readAt: ZonedDateTime? = null,
)
