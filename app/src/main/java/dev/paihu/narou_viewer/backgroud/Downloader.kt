package dev.paihu.narou_viewer.backgroud

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.paihu.narou_viewer.network.NarouService

class Downloader(
    val ctx: Context, params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val mode = inputData.getString("mode")
        if (mode == "novel") {
            inputData.getString("novelId")?.let { novelId ->
                val novel = NarouService.getNovelInfo(novelId.lowercase())
                val pages = NarouService.getPagesInfo(novelId.lowercase())

                val manager = WorkManager.getInstance(ctx)
                pages.forEach {
                    manager.enqueueUniqueWork(
                        "narou", ExistingWorkPolicy.APPEND_OR_REPLACE,
                        OneTimeWorkRequestBuilder<Downloader>()
                            .addTag("narou")
                            .setInputData(
                                workDataOf(
                                    "type" to "narou",
                                    "mode" to "page",
                                    "novelId" to it.novelId,
                                    "pageId" to it.pageId
                                )
                            ).build()
                    )
                }
                return Result.success()
            }
            Log.w("downloader-novel", "variable not found")
        } else {
            inputData.getString("novelId")?.let { novelId ->
                inputData.getString("pageId")?.let { pageId ->
                    val page = NarouService.getPage(novelId, pageId)
                    Log.i("downloader-page", page)
                    Thread.sleep(1000)
                    return Result.success()
                }
            }
            Log.w("downloader-page", "variable not found")
        }
        return Result.failure()
    }

}