package dev.paihu.narou_viewer.network

import dev.paihu.narou_viewer.model.Novel

interface SearchService {
    suspend fun search(word: String, st: Int? = null, limit: Int? = null): List<Novel>

    suspend fun getNovelInfo(novelId: String): Novel


    suspend fun getPagesInfo(novelId: String): List<PageInfo>

    suspend fun getPage(novelId: String, pageId: String): String
}