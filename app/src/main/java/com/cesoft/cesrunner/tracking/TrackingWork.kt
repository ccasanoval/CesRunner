package com.cesoft.cesrunner.tracking

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.Duration

class TrackingWork(
    appContext: Context,
    workerParams: WorkerParameters
): Worker(appContext, workerParams) {

    override fun doWork(): Result {
        android.util.Log.e(TAG, "doWork---------------------------------------")
        return Result.success()
    }

    companion object {
        private const val TAG = "TrackingWork"
        fun create(appContext: Context) {
            //The repeat interval must be greater than or equal to MIN_PERIODIC_INTERVAL_MILLIS
            //androidx.work.PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS == 15min
            val workReq = PeriodicWorkRequestBuilder<TrackingWork>(Duration.ofMinutes(1))
            WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                workReq.build(),
            )
        }
    }
}