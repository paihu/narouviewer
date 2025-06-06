package dev.paihu.narou_viewer.network

import android.net.Uri
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.paihu.narou_viewer.data.Novel
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.ParseException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Narou18SearchPagingSource(val query: String) : PagingSource<Int, Novel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Novel> {
        if (query.isEmpty()) return LoadResult.Page(
            data = emptyList(),
            prevKey = null,
            nextKey = null
        )
        val st = params.key ?: 0
        val limit = params.loadSize
        val ret = Narou18Service.search(query, st, limit = limit)
        val prevKey = st - limit
        return LoadResult.Page(
            data = ret,
            prevKey = if (prevKey <= 0) null else prevKey,
            nextKey = if (ret.size < limit) null else st + limit
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Novel>): Int? {
        return null
    }
}
interface Narou18SearchApi {
    @GET("/novel18api/api")
    suspend fun searchNovels(
        @Query("word") word: String? = null,
        @Query("st") st: Int? = null,
        @Query("out") out: String = "json",
        @Query("title") title: Int = 1,
        @Query("of") of: String = "t-n-w-nu-gf",
        @Query("lim") limit: Int? = null
    ): Array<NarouSearchResult>

    @GET("/novel18api/api")
    suspend fun fetchNovelInfo(
        @Query("ncode") ncode: String? = null,
        @Query("out") out: String = "json",
        @Query("of") of: String = "t-n-w-nu-gf"
    ): Array<NarouSearchResult>
}
object Narou18Service: SearchService{
    override val host = "novel18.syosetu.com"
    override val type = "narou18"
    private val client by lazy{
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Pixel 6)")
                    .addHeader("Cookie", "over18=yes")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private val fetchService by lazy {
        Retrofit.Builder()
            .baseUrl("https://${host}")
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(client)
            .build()
            .create(NarouApi::class.java)
    }
    private val searchService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.syosetu.com")
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                )
            )
            .client(client)
            .build()
            .create(Narou18SearchApi::class.java)
    }


    override suspend fun search(word: String, st: Int?, limit: Int?): List<Novel> {
        val ret = searchService.searchNovels(word, st, limit = limit)
        return ret.mapNotNull { resultToNovel(it) }
    }

    override fun getNovelId(uri: Uri): String? {
        return "https://${host}/([^/]+)".toRegex().find(uri.toString())?.groupValues?.get(
            1
        )

    }

    override suspend fun getNovelInfo(novelId: String): Novel {
        return searchService.fetchNovelInfo(novelId).mapNotNull { resultToNovel(it) }.first()
    }

    override suspend fun getPagesInfo(novelId: String): List<PageInfo> {
        val ret = Jsoup.parse(fetchService.fetchNovelPagesInfo(novelId))
        val info =
            ret.select("div.p-eplist__sublist").map { elementToPageInfo(it, novelId) }
                .toMutableList()
        return try {
            for (i in 2..ret.select("a.c-pager__item--last")[0].attr("href")
                .split("=")[1].toInt()) {
                val res = Jsoup.parse(fetchService.fetchNovelPagesInfo(novelId, i))
                val addInfo =
                    res.select("div.p-eplist__sublist").map { elementToPageInfo(it, novelId) }
                info.addAll(addInfo)
            }
            info.toList()
        } catch (e: IndexOutOfBoundsException) {
            info.toList()
        }
    }

    override suspend fun getPage(novelId: String, pageId: String): String {
        val ret = Jsoup.parse(fetchService.fetchPageData(novelId, pageId))
        return ret.select(".p-novel__text > p").joinToString("\n") { it.text() }
    }

    private fun elementToPageInfo(it: Element, novelId: String): PageInfo {
        val title = it.select("a").text()
        val pageId = it.select("a").attr("href").split("/")[2]
        val createdAt = it.select(".p-eplist__update")[0].ownText().replace("\"", "").trim()
        val info = PageInfo(
            title = title,
            pageId = pageId,
            pageNum = pageId.toInt(),
            novelId = novelId,
            updatedAt = createdAt.toZoneDateTime() ?: ZonedDateTime.now(),
            createdAt = createdAt.toZoneDateTime() ?: ZonedDateTime.now(),
        )
        return try {
            val updatedAt =
                it.select(".p-eplist_update > span")[0].attr("title").replace("改稿", "").trim()
                    .toZoneDateTime()
            info.copy(updatedAt = updatedAt!!)
        } catch (e: IndexOutOfBoundsException) {
            info
        }
    }

    private fun resultToNovel(result: NarouSearchResult): Novel? {
        if (result.allcount !== null) return null
        return Novel(
            title = result.title!!,
            author = result.writer!!,
            novelId = result.ncode!!.lowercase(),
            type = type,
            updatedAt = result.novelupdated_at!!.toZoneDateTime("yyyy-MM-dd HH:mm:ss")!!,
            createdAt = result.general_firstup!!.toZoneDateTime("yyyy-MM-dd HH:mm:ss")!!,
        )
    }

    private fun String.toZoneDateTime(pattern: String = "yyyy/MM/dd HH:mm"): ZonedDateTime? {
        val formatter = try {
            DateTimeFormatter.ofPattern(pattern)
        } catch (e: IllegalArgumentException) {
            null
        }
        val date = formatter?.let {
            try {
                ZonedDateTime.of(LocalDateTime.parse(this, formatter), ZoneId.of("Asia/Tokyo"))
            } catch (e: ParseException) {
                null
            }
        }
        return date
    }
}