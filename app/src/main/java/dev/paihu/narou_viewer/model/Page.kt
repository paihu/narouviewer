package dev.paihu.narou_viewer.model

data class Page(
    val id: Int,
    val novelId: Int,
    val pageNum: Int,
    val title: String,
    val pageText: String
)
