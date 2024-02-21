package dev.paihu.narou_viewer.network

import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.paihu.narou_viewer.model.Novel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.JsonTreeBuilder
import org.jsoup.select.Elements
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.ZonedDateTime


interface KakuyomuApi {
    @GET("/works/{novelId}/")
    suspend fun fetchNovelPagesInfo(
        @Path("novelId") novelId: String,
    ): String

    @GET("/works/{novelId}/episodes/{pageId}")
    suspend fun fetchPageData(
        @Path("novelId") novelId: String,
        @Path("pageId") pageId: String,
    ): String

    @GET("/search")
    suspend fun searchNovels(
        @Query("q") qury: String,
        @Query("page") page: Int? = null,
    ): String
}


class KakuyomuPagingSource(val query: String) : PagingSource<Int, Novel>() {
    private val perPage = 20
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Novel> {
        if (query.isEmpty()) return LoadResult.Page(
            data = emptyList(),
            prevKey = null,
            nextKey = null
        )
        val st = params.key ?: 0
        val limit = params.loadSize
        val novels = mutableListOf<Novel>()
        val prevKey = st - limit
        var page = st / perPage
        val offset = st % perPage
        novels.addAll(KakuyomuService.search(query, page).drop(offset))
        page += 1
        while (novels.size < limit) {
            val results = KakuyomuService.search(query, page)
            novels.addAll(results)
            if (results.size < perPage) break
            page += 1
        }
        return LoadResult.Page(
            data = novels.take(limit),
            prevKey = if (prevKey <= 0) null else prevKey,
            nextKey = if (novels.size < limit) null else st + limit
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Novel>): Int? {
        TODO("Not yet implemented")
    }
}

object KakuyomuService : SearchService {
    private val fetchService by lazy {
        Retrofit.Builder()
            .baseUrl("https://kakuyomu.jp/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(KakuyomuApi::class.java)
    }

    override suspend fun search(word: String, page: Int?, limit: Int?): List<Novel> {
        val novels = mutableListOf<Novel>()
        val ret = Jsoup.parse(fetchService.searchNovels(word, page))
        val jsonTree = Jsoup.parse(
            ret.select("script#__NEXT_DATA__")[0].dataNodes().last().toString(),
            JsonTreeBuilder.jsonParser()
        )
        val nodes = jsonTree.select("#ROOT_QUERY #nodes > obj")
        nodes.forEach {
            if (it.text().startsWith("Work")) {
                val title = jsonTree.select("#${it.text().replace(":", "\\:")} #title").text()
                val createdAt = ZonedDateTime.parse(
                    jsonTree.select(
                        "#${
                            it.text().replace(":", "\\:")
                        } #publishedAt"
                    ).text()
                )
                val updatedAt = ZonedDateTime.parse(
                    jsonTree.select(
                        "#${
                            it.text().replace(":", "\\:")
                        } #lastEpisodePublishedAt"
                    ).text()
                )
                val author = jsonTree.select(
                    "#${
                        jsonTree.select("#${it.text().replace(":", "\\:")} #author").text()
                            .replace(":", "\\:")
                    } #activityName"
                ).text()
                val novelId = it.text().split(":").last()
                novels.add(
                    Novel(
                        title = title,
                        author = author,
                        novelId = novelId,
                        type = "kakuyomu",
                        updatedAt = updatedAt,
                        createdAt = createdAt,
                    )
                )
            }
        }
        return novels
    }

    override suspend fun getNovelInfo(novelId: String): Novel {
        val ret = Jsoup.parse(fetchService.fetchNovelPagesInfo(novelId))
        val jsonTree = Jsoup.parse(
            ret.select("script#__NEXT_DATA__")[0].dataNodes().last().toString(),
            JsonTreeBuilder.jsonParser()
        )
        val novelQueryId = "#Work\\:$novelId"
        val title = jsonTree.select("$novelQueryId #title").text()

        val createdAt = ZonedDateTime.parse(
            jsonTree.select(
                "$novelQueryId #publishedAt"
            ).text()
        )
        val updatedAt = ZonedDateTime.parse(
            jsonTree.select(
                "$novelQueryId #lastEpisodePublishedAt"
            ).text()
        )
        val author = jsonTree.select(
            "#${
                jsonTree.select("$novelQueryId #author").text()
                    .replace(":", "\\:")
            } #activityName"
        ).text()
        return Novel(
            title = title,
            author = author,
            novelId = novelId,
            type = "kakuyomu",
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    override suspend fun getPagesInfo(novelId: String): List<PageInfo> {
        val ret = Jsoup.parse(fetchService.fetchNovelPagesInfo(novelId))
        val jsonTree = Jsoup.parse(
            ret.select("script#__NEXT_DATA__")[0].dataNodes().last().toString(),
            JsonTreeBuilder.jsonParser()
        )
        val rootContents = jsonTree.select("#Work\\:$novelId #tableOfContents")
        return scrapePageInfo(
            novelId,
            jsonTree,
            rootContents
        ).mapIndexed { index, pageInfo -> return listOf(pageInfo.copy(pageNum = index + 1)) }
    }

    override suspend fun getPage(novelId: String, pageId: String): String {
        val ret = Jsoup.parse(fetchService.fetchPageData(novelId, pageId))
        return ret.select(".widget-episodeBody p").map { it.text() }.joinToString("\n")
    }

    private fun scrapePageInfo(
        novelId: String,
        root: Document,
        contents: Elements
    ): List<PageInfo> {
        return contents.map {
            val id = it.text().replace(":", "\\:")
            return if (id.startsWith("TableOfContentsChapter")) {
                scrapePageInfo(novelId, root, root.select("#$id #episodeUnions"))
            } else {
                val title = root.select("#$id #title").text()
                val createdAt = ZonedDateTime.parse(root.select("#$id #publishedAt").text())
                return listOf(
                    PageInfo(
                        novelId = novelId,
                        pageId = id.split(":").last(),
                        title = title,
                        pageNum = 0,
                        createdAt = createdAt,
                        updatedAt = createdAt
                    )
                )
            }
        }

    }
}
