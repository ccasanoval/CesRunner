package com.cesoft.cesrunner.tracking

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cesoft.cesrunner.domain.usecase.ReadLastTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadVo2MaxUC
import com.cesoft.cesrunner.domain.usecase.SaveVo2MaxUC
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StopTrackingVo2MaxWork(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams), KoinComponent {

    private val readLastTrackUC: ReadLastTrackUC by inject()
    private val readVo2MaxUC: ReadVo2MaxUC by inject()
    private val saveVo2MaxUC: SaveVo2MaxUC by inject()

    override suspend fun doWork(): Result {
        readLastTrackUC().getOrNull()?.let { track ->
            val vo2Max = track.calcVo2Max()
            if(vo2Max > readVo2MaxUC()) {
                saveVo2MaxUC(vo2Max)//TODO: Save also the id of the track with the record?
            }
        }
        return Result.success()
    }

    companion object {
        fun create(context: Context) {
            val workReq = OneTimeWorkRequestBuilder<StopTrackingVo2MaxWork>()
            WorkManager.getInstance(context).enqueue(workReq.build())
        }
    }
}