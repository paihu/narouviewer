package dev.paihu.narou_viewer.network

import android.net.Uri
import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.paihu.narou_viewer.model.Novel
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.time.ZonedDateTime


interface KakuyomuApi {
    @GET("/works/{novelId}")
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
        @Query("q") query: String,
        @Query("page") page: Int? = null,
    ): String
}


class KakuyomuPagingSource(private val query: String) : PagingSource<Int, Novel>() {
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
            val results = KakuyomuService.search(query, page)
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

object KakuyomuService : SearchService {
    override val host = "kakuyomu.jp"
    override val type = "kakuyomu"

    private val fetchService by lazy {
        Retrofit.Builder()
            .baseUrl("https://$host/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(KakuyomuApi::class.java)
    }

    /*
     * sample response script data
       {
                "props": {
                    "pageProps": {
                        "initAdjustableWorkItemSize": "full",
                        "__REDUX_STATE__": {
                            "tier": "regular",
                            "platform": "web"
                        },
                        "__MAINTENANCE_BLIND_STATE__": {
                            "activated": false
                        },
                        "__APOLLO_STATE__": {
                            "ROOT_QUERY": {
                                "__typename": "Query",
                                "visitor": null,
                                "canShowFeature({\"name\":\"work-toc-expanded\"})": true,
                                "canShowFeature({\"name\":\"kakuyomu-next-pre-registration\"})": true,
                                "canShowFeature({\"name\":\"payment-maintenance\"})": false,
                                "canShowFeature({\"name\":\"renewal-work-page\"})": true,
                                "canShowFeature({\"name\":\"user-mute-web\"})": false,
                                "canShowFeature({\"name\":\"kakuyomu-contest-selection\"})": false,
                                "canShowFeature({\"name\":\"update_management_ver1\"})": false,
                                "canShowFeature({\"name\":\"next-media-2nd\"})": false,
                                "canShowFeature({\"name\":\"kakuyomu-next\"})": false,
                                "searchWorks({\"exclusionConditions\":[],\"first\":20,\"genres\":[],\"inclusionConditions\":[],\"offset\":0,\"order\":\"WEEKLY_RANKING\",\"query\":\"蜘蛛\"})": {
                                    "__typename": "SearchWorkConnection",
                                    "nodes": [
                                        {
                                            "__ref": "Work:16817330657705436671"
                                        },
                                        {
                                            "__ref": "Work:16818023213827520038"
                                        },
                                        *                                         {
                                            "__ref": "Work:16818023212775091727"
                                        }
                                    ],
                                    "pageInfo": {
                                        "__typename": "PageInfo",
                                        "hasNextPage": true,
                                        "hasPreviousPage": false
                                    },
                                    "totalCount": 375
                                },
                                                            "Work:16817330657705436671": {
                                "__typename": "Work",
                                "id": "16817330657705436671",
                                "baseColor": "#F6BE48",
                                "catchphrase": "蜘蛛と言えばスパイダーシルク。布があるなら裁縫するっきゃないよね！",
                                "author": {
                                    "__ref": "UserAccount:1177354054886263230"
                                },
                                "textualWorkReviewsForCatchphraseInRandom": [
                                    {
                                        "__ref": "TextualWorkReview:16817330661971957244"
                                    },
                                    {
                                        "__ref": "TextualWorkReview:16817330662047064226"
                                    }
                                ],
                                "title": "私、蜘蛛なモンスターをテイムしたのでスパイダーシルクで裁縫を頑張ります！",
                                "alternateTitle": null,
                                "isFreshWork": false,
                                "hasFreshEpisode": false,
                                "publishedAt": "2023-06-03T03:10:12Z",
                                "lastEpisodePublishedAt": "2024-02-23T03:17:59Z",
                                "alternateAuthorName": null,
                                "introduction": "【第5回ドラゴンノベルス小説コンテスト大賞受賞！】\n\nとある世界で死んでしまった少女。彼女は別の世界の女神から祝福され、その女神の世界によみがえる。そこで与えられたのは裁縫にまつわる技術を使うか再び輪廻の輪に戻るかという選択肢。\nせっかく新しい人生が与えられたなら楽しまなくちゃ損だよね！\n\n女神様に与えられた小屋で武器や魔法の扱い方を覚えた少女《リリィ》は、特大登山用リュックを背負って小屋に別れを告げ人里を目指す。\n途中、ラージシルクスパイダーのタラトを仲間にし蜘蛛糸が取れるようになった彼女は、蜘蛛糸から作られるスパイダーシルクを売って生計を立てていこうと心に誓う。\n途中、冒険者ギルドや商業ギルドのお世話になりながらも、自分のお店目指して頑張るリリィ。\n当面の目標は自分のお店を持つこと！\n\n※この作品は現在カクヨム様でのみ公開しております\n　その他のサイトでは公開しておりません",
                                "totalReviewPoint": 12157,
                                "fanFictionSource": null,
                                "genre": "FANTASY",
                                "serialStatus": "RUNNING",
                                "publicEpisodeCount": 300,
                                "totalCharacterCount": 459920,
                                "totalFollowers": 20769,
                                "totalPublicEpisodeCommentCount": 2577,
                                "isCruel": true,
                                "isViolent": false,
                                "isSexual": false,
                                "tagLabels": [
                                    "女主人公",
                                    "スローライフ",
                                    "テイマー",
                                    "裁縫",
                                    "万人向け",
                                    "物作り",
                                    "お仕事",
                                    "ひたむきヒロイン部門"
                                ],
                                "firstPublicEpisodeUnion": {
                                    "__ref": "Episode:16817330657705469261"
                                },
                                "visitorWorkFollowing": null,
                                "kakuyomuNextWork": null
                            },
                                                        "RecommendedKeywordSection:16817330652999909646": {
                                "__typename": "RecommendedKeywordSection",
                                "id": "16817330652999909646",
                                "title": "運営のおすすめ",
                                "keywords": [
                                    "カクヨムオンリー",
                                    "銃",
                                    "怪異",
                                    "民俗学",
                                    "京都",
                                    "バディ",
                                    "グルメ",
                                    "インターネット文学",
                                    "ブロマンス"
                                ]
                            }
                        },
                        "_sentryTraceData": "c797ebf9fece45a79af56eedf19da5d8-a253c3d8a4a23248-0",
                        "_sentryBaggage": "sentry-environment=production,sentry-release=release-94d9c33e0f,sentry-public_key=292a799d6bfc4ebfbe3e651c276b9ac1,sentry-trace_id=c797ebf9fece45a79af56eedf19da5d8,sentry-sample_rate=0,sentry-transaction=%2Fsearch,sentry-sampled=false"
                    },
                    "__N_SSP": true
                },
                "page": "/search",
                "query": {
                    "q": "蜘蛛"
                },
                "buildId": "1f5ukH8wV9EVeLkKeoyQm",
                "isFallback": false,
                "gssp": true,
                "scriptLoader": [
                ]
            }
     */
    override suspend fun search(word: String, st: Int?, limit: Int?): List<Novel> {
        val novels = mutableListOf<Novel>()
        val ret = Jsoup.parse(fetchService.searchNovels(word, st))
        val nextJsonRoot = ret.select("script#__NEXT_DATA__")[0].data()

        val jsonTree = (JSONTokener(
            nextJsonRoot
        ).nextValue() as JSONObject).getJSONObject("props").getJSONObject("pageProps")
            .getJSONObject("__APOLLO_STATE__")

        val nodes = jsonTree.getJSONObject("ROOT_QUERY")
        val queryKey =
            nodes.keys().asSequence().find { it.startsWith("searchWorks") } ?: return novels
        val targets = nodes.getJSONObject(queryKey).getJSONArray("nodes")
        for (i in 0 until targets.length()) {
            val targetId = targets.getJSONObject(i).getString("__ref")
            val novelId = targetId.split(":").last()
            val target = jsonTree.getJSONObject(targetId)
            novels.add(getNovel(novelId, jsonTree, target))
        }
        return novels
    }

    override fun getNovelId(uri: Uri): String? {
        return "https://$host/works/([^/]+)".toRegex().find(uri.toString())?.groupValues?.get(
            1
        )
    }
    override suspend fun getNovelInfo(novelId: String): Novel {
        val ret = Jsoup.parse(fetchService.fetchNovelPagesInfo(novelId))
        val nextJsonRoot = ret.select("script#__NEXT_DATA__")[0].data()

        val jsonTree = (JSONTokener(
            nextJsonRoot
        ).nextValue() as JSONObject).getJSONObject("props").getJSONObject("pageProps")
            .getJSONObject("__APOLLO_STATE__")

        val novelRoot = jsonTree.getJSONObject("Work:$novelId")
        return getNovel(novelId, jsonTree, novelRoot)
    }

    private fun getNovel(novelId: String, root: JSONObject, node: JSONObject): Novel {
        val title = node.getString("title")

        val createdAt = ZonedDateTime.parse(
            node.getString(
                "publishedAt"
            )
        )
        val updatedAt = ZonedDateTime.parse(
            node.getString(
                "lastEpisodePublishedAt"
            )
        )
        val authorId = node.getJSONObject("author").getString("__ref")
        val authorObj = root.getJSONObject(authorId)
        val author = authorObj.getString(
            "activityName"
        )

        return Novel(
            title = title,
            author = author,
            novelId = novelId,
            type = type,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    override suspend fun getPagesInfo(novelId: String): List<PageInfo> {
        val ret = Jsoup.parse(fetchService.fetchNovelPagesInfo(novelId))
        val nextJsonRoot = ret.select("script#__NEXT_DATA__")[0].data()

        val root = (JSONTokener(
            nextJsonRoot
        ).nextValue() as JSONObject).getJSONObject("props").getJSONObject("pageProps")
            .getJSONObject("__APOLLO_STATE__")
        val rootContents = root.getJSONObject("Work:$novelId").getJSONArray("tableOfContents")
        return scrapePageInfo(
            novelId,
            root,
            rootContents
        ).mapIndexed { index, pageInfo -> pageInfo.copy(pageNum = index + 1) }
    }

    override suspend fun getPage(novelId: String, pageId: String): String {
        val ret = Jsoup.parse(fetchService.fetchPageData(novelId, pageId))
        return ret.select(".widget-episodeBody p").joinToString("\n") { it.text() }
    }

    private fun scrapePageInfo(
        novelId: String,
        root: JSONObject,
        contents: JSONArray
    ): List<PageInfo> {
        val pageInfo = mutableListOf<PageInfo>()
        for (i in 0 until contents.length()) {
            val contentId = contents.getJSONObject(i).getString("__ref")
            if (contentId.startsWith("Episode")) {
                val episode = root.getJSONObject(contentId)
                val title = episode.getString("title")
                val id = episode.getString("id")
                val createdAt = ZonedDateTime.parse(episode.getString("publishedAt"))
                pageInfo.add(
                    PageInfo(
                        novelId = novelId,
                        pageNum = 0,
                        title = title,
                        createdAt = createdAt,
                        updatedAt = createdAt,
                        pageId = id
                    )
                )
            } else {
                pageInfo.addAll(
                    scrapePageInfo(
                        novelId,
                        root,
                        root.getJSONObject(contentId).getJSONArray("episodeUnions")
                    )
                )
            }
        }
        return pageInfo
    }
}
