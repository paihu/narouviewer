package dev.paihu.narou_viewer.model

import java.time.ZonedDateTime

data class Novel(
    val id: Int? = null,
    val title: String,
    val author: String,
    val novelId: String?,
    val type: String,
    val createdAt: ZonedDateTime? = null,
    val updatedAt: ZonedDateTime? = null,
)
