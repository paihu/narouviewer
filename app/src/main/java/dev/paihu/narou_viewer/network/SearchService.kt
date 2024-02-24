package dev.paihu.narou_viewer.network

import android.net.Uri
import dev.paihu.narou_viewer.data.Novel

interface SearchService {
    val host: String
    val type: String
    suspend fun search(word: String, st: Int? = null, limit: Int? = null): List<Novel>

    suspend fun getNovelInfo(novelId: String): Novel

    fun getNovelId(uri: Uri): String?


    suspend fun getPagesInfo(novelId: String): List<PageInfo>

    suspend fun getPage(novelId: String, pageId: String): String
}