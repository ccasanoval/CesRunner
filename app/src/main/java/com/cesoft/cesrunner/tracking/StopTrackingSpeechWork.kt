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
import com.cesoft.cesrunner.toTimeSpeech

class StopTrackingSpeechWork(
    private val appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

    private lateinit var textToSpeech: TextToSpeech
//    private fun speak(text: String) =
//        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, appContext.packageName)

    private suspend fun stopSpeech() {
        readLastTrack().getOrNull()?.let { track ->
            textToSpeech = TextToSpeech(applicationContext) { status ->
                if(status == TextToSpeech.SUCCESS) {
                    val k = appContext.getString(R.string.kilometers)
                    val text = appContext.getString(R.string.stop_tracking) + ". " +
                            appContext.getString(R.string.distance) +
                            "${(track.distance/100)/10f} $k. " +
                            appContext.getString(R.string.time) +
                            (track.timeEnd - track.timeIni).toTimeSpeech(appContext)
                    textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, appContext.packageName)
                    textToSpeech.setOnUtteranceProgressListener(
                        object: UtteranceProgressListener() {
                        override fun onStart(p0: String?) {}
                        override fun onDone(p0: String?) {
                            textToSpeech.shutdown()
                        }
                        @Deprecated("") override fun onError(p0: String?) {}
                    })
                }
            }
        }
    }

    override suspend fun doWork(): Result {
        stopSpeech()
        return Result.success()
    }

    companion object {
        private const val TAG = "TrackingWork"
        lateinit var readLastTrack: ReadLastTrackUC
        fun create(context: Context, track: ReadLastTrackUC) {
            readLastTrack = track
            val workReq = OneTimeWorkRequestBuilder<StopTrackingSpeechWork>()
            WorkManager.getInstance(context).enqueue(workReq.build())
        }
    }
}