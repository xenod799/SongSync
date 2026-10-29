package com.songsync.media.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.songsync.media.scanner.MediaStoreScanner
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class MediaSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scanner: MediaStoreScanner
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            setProgress(workDataOf(KEY_PROGRESS to 0))
            scanner.scanAndSync { progressPercent ->
                setProgress(workDataOf(KEY_PROGRESS to progressPercent))
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME_ONE_TIME = "media_sync_work_one_time"
        const val WORK_NAME_PERIODIC = "media_sync_work_periodic"
        const val KEY_PROGRESS = "sync_progress"

        fun enqueueOneTimeSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<MediaSyncWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun enqueuePeriodicSync(context: Context) {
            val request = PeriodicWorkRequestBuilder<MediaSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
