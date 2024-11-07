package com.cesoft.cesrunner.tracking

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackUC
import com.cesoft.cesrunner.domain.usecase.ReadLastTrackUC
import com.cesoft.cesrunner.toTimeSpeech
import kotlinx.coroutines.delay

class StopTrackingSpeechWork(
    private val appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

    //private val readCurrentTracking: ReadCurrentTrackingUC by inject()
    private lateinit var textToSpeech: TextToSpeech
    private fun speak(text: String) =
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, appContext.packageName)

    private suspend fun stopSpeech() {
        readLastTrack().getOrNull()?.let { track ->
            textToSpeech = TextToSpeech(applicationContext) { status ->
                if(status == TextToSpeech.SUCCESS) {
//                    speak(appContext.getString(R.string.stop_tracking))
//                    android.util.Log.e("AAAA", "-------------- A ")
//                    val k = appContext.getString(R.string.kilometers)
//                    speak(appContext.getString(R.string.distance))
//                    speak("${(track.distance/100)/10f} $k")
//                    speak(appContext.getString(R.string.time))
//                    speak((track.timeEnd - track.timeIni).toTimeSpeech(appContext))
//                    android.util.Log.e("AAAA", "-------------- B ")
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
            //delay(10_000)
            //textToSpeech.shutdown()
        }
    }

    override suspend fun doWork(): Result {
        android.util.Log.e(TAG, "doWork---------------------------------------")
        stopSpeech()
        return Result.success()
    }

    companion object {
        private const val TAG = "TrackingWork"
        lateinit var readLastTrack: ReadLastTrackUC
        fun create(appContext: Context, v: ReadLastTrackUC) {
            readLastTrack = v
            val workReq = OneTimeWorkRequestBuilder<StopTrackingSpeechWork>()
            WorkManager.getInstance(appContext).enqueue(workReq.build())
        }
    }
}