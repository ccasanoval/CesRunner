package com.cesoft.cesrunner.tracking

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.usecase.ReadLastTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadVo2MaxUC
import com.cesoft.cesrunner.domain.usecase.SaveVo2MaxUC
import com.cesoft.cesrunner.toTimeSpeech

class StopTrackingVo2MaxWork(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

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
        private const val TAG = "StopTrackingVo2MaxWork"
        lateinit var readLastTrackUC: ReadLastTrackUC
        lateinit var readVo2MaxUC: ReadVo2MaxUC
        lateinit var saveVo2MaxUC: SaveVo2MaxUC
        fun create(
            context: Context,
            readLastTrack: ReadLastTrackUC,
            readVo2Max: ReadVo2MaxUC,
            saveVo2Max: SaveVo2MaxUC
        ) {
            readLastTrackUC = readLastTrack
            readVo2MaxUC = readVo2Max
            saveVo2MaxUC = saveVo2Max
            val workReq = OneTimeWorkRequestBuilder<StopTrackingVo2MaxWork>()
            WorkManager.getInstance(context).enqueue(workReq.build())
        }
    }
}