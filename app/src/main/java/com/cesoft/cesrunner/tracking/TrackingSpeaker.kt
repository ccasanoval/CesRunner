package com.cesoft.cesrunner.tracking

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.cesoft.cesrunner.domain.usecase.ReadSettingsUC

/// Text to speech for the tracking service, only speaks when the user enabled the voice in settings
class TrackingSpeaker(
    private val context: Context,
    private val readSettings: ReadSettingsUC,
) {
    private val textToSpeech = TextToSpeech(context) { status ->
        if(status != TextToSpeech.SUCCESS) {
            Log.e(TAG, "TextToSpeech: Initialization Failed: $status")
        }
    }

    suspend fun speak(text: String) {
        if(readSettings().getOrNull()?.voice == true) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, context.packageName)
        }
    }

    fun shutdown() {
        textToSpeech.shutdown()
    }

    companion object {
        private const val TAG = "TrackingSpeaker"
    }
}
