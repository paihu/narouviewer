package dev.paihu.narou_viewer.model

import java.time.LocalDateTime

data class Page(
    val id: Int,
    val novelId: Int,
    val pageNum: Int,
    val title: String,
    val pageText: String,
    val updateAt: LocalDateTime?,
)
