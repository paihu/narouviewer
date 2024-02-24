package dev.paihu.narou_viewer.backgroud

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.paihu.narou_viewer.data.Novel
import dev.paihu.narou_viewer.data.Page
import dev.paihu.narou_viewer.data.initDb
import dev.paihu.narou_viewer.network.KakuyomuService
import dev.paihu.narou_viewer.network.NarouService
import dev.paihu.narou_viewer.network.SearchService
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class Downloader(
    private val ctx: Context, params: WorkerParameters
) : CoroutineWorker(ctx, params) {
    val db by lazy { initDb(ctx) }

    override suspend fun doWork(): Result {
        val mode = inputData.getString("mode")
        return if (mode == "novel") {
            doNovel(inputData)
        } else {
            doPage(inputData)
        }
    }

    private fun getService(type: String): SearchService? {
        return when (type) {
            KakuyomuService.type -> KakuyomuService
            NarouService.type -> NarouService
            else -> null
        }
    }

    private suspend fun doNovel(inputData: Data): Result {
        val novelId = inputData.getString("novelId") ?: return Result.failure()
        val type = inputData.getString("type") ?: return Result.failure()
        val service = getService(type) ?: return Result.failure()
        val novelInfo = service.getNovelInfo(novelId.lowercase())
        val novel = db.novelDao().select(novelInfo.novelId!!, type)?.copy(
            title = novelInfo.title,
            author = novelInfo.author,
            updatedAt = novelInfo.updatedAt ?: novelInfo.createdAt!!
        ) ?: Novel(
            novelId = novelInfo.novelId,
            type = type,
            title = novelInfo.title,
            author = novelInfo.author,
            updatedAt = novelInfo.updatedAt ?: ZonedDateTime.now(),
            createdAt = ZonedDateTime.now(),
        )
        db.novelDao().upsert(
            novel
        )

        val pagesInfo = service.getPagesInfo(novelId.lowercase())

        val pages = db.pageDao().getAll(novel.novelId, type)
        db.close()
        val targets = pagesInfo.filter { info ->
            pages.find { it.num == info.pageNum && info.updatedAt < it.downloadedAt } == null
        }
        Log.i("downloader", "targetCount ${targets.count()}")
        val manager = WorkManager.getInstance(ctx)
        targets.forEach {
            manager.enqueueUniqueWork(
                "narou", ExistingWorkPolicy.APPEND_OR_REPLACE,
                OneTimeWorkRequestBuilder<Downloader>()
                    .addTag("narou")
                    .setInputData(
                        workDataOf(
                            "type" to type,
                            "mode" to "page",
                            "novelId" to it.novelId,
                            "pageId" to it.pageId,
                            "pageNum" to it.pageNum,
                            "title" to it.title,
                            "updatedAt" to it.updatedAt.toEpochSecond(),
                            "createdAt" to it.createdAt.toEpochSecond(),
                        )
                    ).build()
            )
        }
        return Result.success()
    }

    private suspend fun doPage(inputData: Data): Result {
        val novelId = inputData.getString("novelId") ?: return Result.failure()
        val type = inputData.getString("type") ?: return Result.failure()
        val pageId = inputData.getString("pageId") ?: return Result.failure()
        val title = inputData.getString("title") ?: return Result.failure()
        val updatedAt = inputData.getLong("updatedAt", 0)
        val createdAt = inputData.getLong("createdAt", 0)
        val pageNum = inputData.getInt("pageNum", 0)
        if (pageNum == 0) return Result.failure()
        val service = getService(type) ?: return Result.failure()

        val page = db.pageDao().select(novelId, type, pageNum)?.copy(
            title = title,
            pageId = pageId,
        ) ?: Page(
            pageId = pageId,
            num = pageNum,
            novelId = novelId,
            novelType = type,
            title = title,

            createdAt = ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(createdAt),
                ZoneId.systemDefault()
            ),
            updatedAt = ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(updatedAt),
                ZoneId.systemDefault()
            ),
        )
        if (updatedAt <= (page.downloadedAt?.toEpochSecond() ?: 0)) {
            db.close()
            return Result.success()
        }

        val pageInfo = service.getPage(novelId, pageId)
        db.pageDao().upsert(
            page.copy(
                content = pageInfo,
                updatedAt = ZonedDateTime.ofInstant(
                    Instant.ofEpochSecond(updatedAt),
                    ZoneId.systemDefault()
                ),
                downloadedAt = ZonedDateTime.now()
            )
        )
        db.close()
        Log.i("downloader-page", "finish ${page.novelId}/${page.num} ${page.title}")
        delay(1000)
        return Result.success()
    }
}