package dev.paihu.narou_viewer.network

import android.net.Uri
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.paihu.narou_viewer.data.Novel
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.json.JSONTokener
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CopyOnWriteArrayList

class MemoryCookieJar : CookieJar {
    private val cookies = CopyOnWriteArrayList<Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        synchronized(this) {
            cookies.forEach { newCookie ->
                this.cookies.removeAll { old ->
                    old.name == newCookie.name &&
                            old.domain == newCookie.domain &&
                            old.path == newCookie.path
                }
                if (newCookie.expiresAt > System.currentTimeMillis()) {
                    this.cookies.add(newCookie)
                }
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        synchronized(this) {
            val now = System.currentTimeMillis()
            return cookies.filter { cookie ->
                cookie.expiresAt > now && cookie.matches(url)
            }
        }
    }

    fun clear() {
        synchronized(this) {
            this.cookies.clear()
        }
    }
}


class AlphapolisPagingSource(private val query: String) : PagingSource<Int, Novel>() {
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
        while (novels.size < limit) {
            val results = AlphapolisService.search(query, page)
            if (novels.size == 0) {
                novels.addAll(results.drop(offset))
            } else {
                novels.addAll(results)
            }
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
        return null
    }
}

interface AlphapolisApi {
    @GET("/novel/{novelId}")
    suspend fun fetchNovelPagesInfo(
        @Path("novelId", encoded = true) novelId: String,
    ): String

    @GET("/novel/{novelId}/episode/{pageId}")
    suspend fun fetchPageData(
        @Path("novelId", encoded = true) novelId: String,
        @Path("pageId") pageId: String,
    ): String

    @GET("/search")
    suspend fun searchNovels(
        @Query("query") query: String,
        @Query("page") page: Int? = null,
    ): String
}


object AlphapolisService : SearchService {
    override val host = "www.alphapolis.co.jp"
    override val type = "alphapolis"
    override val displayName = "Alphapolis"
    var cookieJar =
        MemoryCookieJar()


    val client by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 6)")
                    .build()
                chain.proceed(request)
            }
            .build()
    }
    private val fetchService by lazy {
        Retrofit.Builder()
            .baseUrl("https://${AlphapolisService.host}/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(client)
            .build()
            .create(AlphapolisApi::class.java)
    }

    override suspend fun search(word: String, st: Int?, limit: Int?): List<Novel> {
        val page = if (st != null && limit != null) (st / limit) + 1 else st
        val ret = Jsoup.parse(fetchService.searchNovels(word, page))
        val novels = ret.select(".section.novels.content-block")
        return novels.map { element ->
            val titleLink = element.selectFirst(".content-title .title a")
            val title = titleLink?.text()?.trim() ?: ""
            val href = titleLink?.attr("href") ?: ""
            val novelId = href.removePrefix("/novel/").trim('/')

            val author = element.selectFirst(".author a")?.text()?.trim() ?: ""

            val updatedStr =
                element.selectFirst(".updated")?.text()?.replace("最終更新日", "")?.trim() ?: ""
            val createdStr =
                element.selectFirst(".created")?.text()?.replace("登録日", "")?.trim() ?: ""

            Novel(
                novelId = novelId,
                author = author,
                type = type,
                title = title,
                updatedAt = updatedStr.toAlphapolisZonedDateTime(),
                createdAt = createdStr.toAlphapolisZonedDateTime()
            )
        }
    }

    override fun getNovelId(uri: Uri): String? {
        if (uri.host != host) return null
        val path = uri.path ?: return null
        return "^/novel/([^/]+/[^/]+)".toRegex().find(path)?.groupValues?.get(1)
    }

    override suspend fun getNovelInfo(novelId: String): Novel {
        val ret = Jsoup.parse(fetchService.fetchNovelPagesInfo(novelId))
        val title = ret.selectFirst("h1.title")?.text()?.trim() ?: ""
        val author = ret.selectFirst(".author a")?.text()?.trim() ?: ""

        val detailTable = ret.selectFirst("table.detail")
        var createdAt = ZonedDateTime.now()
        var updatedAt = ZonedDateTime.now()

        detailTable?.select("tr")?.forEach { tr ->
            val th = tr.selectFirst("th")?.text()
            val td = tr.selectFirst("td")?.text()?.trim()
            if (th != null && td != null) {
                when {
                    th.contains("初回公開日時") -> createdAt = td.toAlphapolisZonedDateTime()
                    th.contains("更新日時") -> updatedAt = td.toAlphapolisZonedDateTime()
                }
            }
        }

        return Novel(
            title = title,
            author = author,
            novelId = novelId,
            type = type,
            updatedAt = updatedAt,
            createdAt = createdAt,
        )
    }

    private fun String.toAlphapolisZonedDateTime(): ZonedDateTime {
        val cleaned = this.trim()
        return try {
            if (cleaned.contains(" ")) {
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")
                LocalDateTime.parse(cleaned, dateTimeFormatter).atZone(ZoneId.of("Asia/Tokyo"))
            } else {
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
                LocalDate.parse(cleaned, dateFormatter).atStartOfDay(ZoneId.of("Asia/Tokyo"))
            }
        } catch (e: Exception) {
            ZonedDateTime.now()
        }
    }

    override suspend fun getPagesInfo(novelId: String): List<PageInfo> {
        val ret = Jsoup.parse(fetchService.fetchNovelPagesInfo(novelId))
        val rawJson = ret.select("script#app-cover-data")[0].data()
        val chapters = (JSONTokener(
            rawJson
        ).nextValue() as JSONObject).getJSONArray("chapterEpisodes")
        val pages = mutableListOf<PageInfo>()
        for (i in 0 until chapters.length()) {
            val episodes = chapters.getJSONObject(i).getJSONArray("episodes")
            for (j in 0 until episodes.length()) {
                val episode = episodes.getJSONObject(j)
                val title = episode.getString("mainTitle")
                val id = episode.getInt("episodeNo")
                val isPublic = episode.getBoolean("isPublic")
                val updatedAt = episode.getString("upTime").toAlphapolisZonedDateTime()
                if (isPublic)
                    pages.add(
                        PageInfo(
                            novelId = novelId,
                            pageId = id.toString(),
                            pageNum = id,
                            title = title,
                            createdAt = updatedAt,
                            updatedAt = updatedAt,
                        )
                    )
            }
        }
        return pages
    }

    override suspend fun getPage(novelId: String, pageId: String): String {
        val ret = Jsoup.parse(fetchService.fetchPageData(novelId, pageId))
        var pageData = "${ret.select("#novelBody")[0]}"
        while (pageData.length < 300) {
            this.cookieJar.clear()
            Log.i(type, "rate limit, sleep 5sec")
            Thread.sleep(5_000)
            getNovelInfo(novelId)
            val ret = Jsoup.parse(fetchService.fetchPageData(novelId, pageId))
            pageData = "${ret.select("#novelBody")[0]}"
        }
        return pageData
    }

    override fun getPagingSource(query: String): PagingSource<Int, Novel> {
        return AlphapolisPagingSource(query)
    }
}