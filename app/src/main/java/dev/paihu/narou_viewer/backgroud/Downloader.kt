package dev.paihu.narou_viewer.backgroud

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dev.paihu.narou_viewer.R
import dev.paihu.narou_viewer.data.Novel
import dev.paihu.narou_viewer.data.Page
import dev.paihu.narou_viewer.data.initDb
import dev.paihu.narou_viewer.network.AlphapolisService
import dev.paihu.narou_viewer.network.KakuyomuService
import dev.paihu.narou_viewer.network.Narou18Service
import dev.paihu.narou_viewer.network.NarouService
import dev.paihu.narou_viewer.network.SearchService
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * TODO: 複数作品を連続でダウンロードする際の安定性向上
 * 現状は作品ごとにタスクを生成しているため、バックグラウンドでの連続実行時にOSの制限を受ける可能性がある。
 * 将来的には、DBに「ダウンロード待ち」フラグを持たせ、1つのタスク内でDBキューを全件消化する仕組みへの変更を検討すること。
 */
class Downloader(
    private val ctx: Context, params: WorkerParameters
) : CoroutineWorker(ctx, params) {
    val db by lazy { initDb(ctx) }

    companion object {
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 1
    }

    override suspend fun doWork(): Result {
        try {
            setForeground(createForegroundInfo("ダウンロードを開始しています..."))
        } catch (e: Exception) {
            Log.e("Downloader", "Failed to set foreground", e)
        }
        val mode = inputData.getString("mode")
        return if (mode == "novel") {
            doNovel(inputData)
        } else {
            doPage(inputData)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo("ダウンロード中...")
    }

    private fun getService(type: String): SearchService? {
        return when (type) {
            KakuyomuService.type -> KakuyomuService
            NarouService.type -> NarouService
            Narou18Service.type -> Narou18Service
            AlphapolisService.type ->
                AlphapolisService

            else -> null
        }
    }

    private suspend fun doNovel(inputData: Data): Result {
        val novelId = inputData.getString("novelId") ?: return Result.failure()
        val type = inputData.getString("type") ?: return Result.failure()
        val service = getService(type) ?: return Result.failure()
        val novelInfo = service.getNovelInfo(novelId.lowercase())
        val novel = db.novelDao().select(novelInfo.novelId, type)?.copy(
            title = novelInfo.title,
            author = novelInfo.author,
            updatedAt = novelInfo.updatedAt
        ) ?: Novel(
            novelId = novelInfo.novelId,
            type = type,
            title = novelInfo.title,
            author = novelInfo.author,
            updatedAt = novelInfo.updatedAt,
            createdAt = ZonedDateTime.now(),
        )
        db.novelDao().upsert(
            novel
        )

        val pagesInfo = service.getPagesInfo(novelId.lowercase())
        val pages = db.pageDao().getAll(novel.novelId, type)

        val targets = pagesInfo.filter { info ->
            val existing = pages.find { it.num == info.pageNum }
            existing == null ||
                    info.updatedAt > (existing.downloadedAt
                ?: ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault())) ||
                    existing.chapterTitle == null
        }

        Log.i("downloader", "targetCount ${targets.count()}")

        targets.forEachIndexed { index, it ->
            val progressText = "${novel.title} (${index + 1} / ${targets.size})"
            try {
                setForeground(createForegroundInfo(progressText))
            } catch (e: Exception) {
                Log.e("Downloader", "Failed to update foreground", e)
            }

            try {
                downloadPageInternal(
                    novelId = it.novelId,
                    type = type,
                    pageId = it.pageId,
                    title = it.title,
                    chapterTitle = it.chapterTitle,
                    updatedAt = it.updatedAt.toEpochSecond(),
                    createdAt = it.createdAt.toEpochSecond(),
                    pageNum = it.pageNum,
                    service = service
                )
                delay(300)
            } catch (e: Exception) {
                Log.e("downloader", "Error downloading page ${it.pageNum}", e)
            }
        }

        db.close()
        return Result.success()
    }

    private suspend fun doPage(inputData: Data): Result {
        val novelId = inputData.getString("novelId") ?: return Result.failure()
        val type = inputData.getString("type") ?: return Result.failure()
        val pageId = inputData.getString("pageId") ?: return Result.failure()
        val title = inputData.getString("title") ?: return Result.failure()
        val chapterTitle = inputData.getString("chapterTitle")
        val updatedAt = inputData.getLong("updatedAt", 0)
        val createdAt = inputData.getLong("createdAt", 0)
        val pageNum = inputData.getInt("pageNum", 0)
        val service = getService(type) ?: return Result.failure()

        downloadPageInternal(
            novelId, type, pageId, title, chapterTitle, updatedAt, createdAt, pageNum, service
        )
        db.close()
        return Result.success()
    }

    private suspend fun downloadPageInternal(
        novelId: String,
        type: String,
        pageId: String,
        title: String,
        chapterTitle: String?,
        updatedAt: Long,
        createdAt: Long,
        pageNum: Int,
        service: SearchService
    ) {
        if (pageNum == 0) return

        val existingPage = db.pageDao().select(novelId, type, pageNum)
        val page = existingPage?.copy(
            title = title,
            pageId = pageId,
            chapterTitle = chapterTitle ?: "",
        ) ?: Page(
            pageId = pageId,
            num = pageNum,
            novelId = novelId,
            novelType = type,
            title = title,
            chapterTitle = chapterTitle ?: "",

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
            if (existingPage != null && existingPage.chapterTitle == null) {
                db.pageDao().upsert(page)
            }
            return
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
        Log.i("downloader-page", "finish $novelId/$pageNum $title")
    }

    private fun createForegroundInfo(contentText: String): ForegroundInfo {
        val title = "作品をダウンロード中"

        val channel = NotificationChannel(
            CHANNEL_ID,
            "ダウンロード通知",
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        return ForegroundInfo(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }
}