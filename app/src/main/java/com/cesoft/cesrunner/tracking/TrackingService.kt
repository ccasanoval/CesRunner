package com.cesoft.cesrunner.tracking

import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.cesoft.cesrunner.R
import com.cesoft.cesrunner.domain.ID_NULL
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.domain.entity.TrackProgress
import com.cesoft.cesrunner.domain.usecase.AddTrackPointUC
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackIdUC
import com.cesoft.cesrunner.domain.usecase.ReadSettingsUC
import com.cesoft.cesrunner.domain.usecase.ReadTrackUC
import com.cesoft.cesrunner.domain.usecase.RequestLocationUpdatesUC
import com.cesoft.cesrunner.toDistanceSpeech
import com.cesoft.cesrunner.toTimeSpeech
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class TrackingService: LifecycleService() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var speaker: TrackingSpeaker

    private val readSettings: ReadSettingsUC by inject()
    private val readCurrentTrackId: ReadCurrentTrackIdUC by inject()
    private val readTrack: ReadTrackUC by inject()
    private val requestLocationUpdates: RequestLocationUpdatesUC by inject()
    private val addTrackPoint: AddTrackPointUC by inject()

    private var trackingJob: Job? = null
    // Kept in memory, so each location doesn't reload the whole track from the database
    private var progress: TrackProgress? = null
    private var speechKm: Float = 0f
    private var countdownDone = false

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        speaker = TrackingSpeaker(applicationContext, readSettings)
    }

    override fun onDestroy() {
        stop()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if(intent?.getBooleanExtra(TrackingNotification.CANCEL_TRACKING, false) == true) {
            stopSelf()// onDestroy() calls stop()
            return START_NOT_STICKY
        }

        //NOTE: !!!
        // Must call startForeground() within 10 seconds of receiving onStartCommand()
        // after starting your service using startForegroundService()
        //https://medium.com/@domen.lanisnik/guide-to-foreground-services-on-android-9d0127dc8f9a
        val notification = TrackingNotification.generateNotification(this, notificationManager)
        startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)

        start()
        // If the system kills the service, it's recreated and reads the current track again
        return START_STICKY
    }

    //https://www.gpxgenerator.com
    private fun start() {
        if(trackingJob?.isActive == true) return // Avoid collecting the locations twice
        trackingJob = lifecycleScope.launch {
            // Settings are read here and not passed from outside, so they survive a service restart
            val settings = readSettings().getOrNull() ?: SettingsDto.Empty
            requestLocationUpdates(
                minInterval = intervalMillis(settings.minInterval),
                minDistance = distanceMeters(settings.minDistance)
            )
                .onEach { point -> onLocation(point) }
                .catch { e ->
                    Log.e(TAG, "start:e: $e")
                    speaker.speak(getString(R.string.error_no_locations))
                    stopSelf()// onDestroy() calls stop()
                }
                .launchIn(this)
        }
    }

    private suspend fun onLocation(point: TrackPointDto) {
        val trackId = readCurrentTrackId().getOrNull() ?: ID_NULL
        if(trackId <= ID_NULL) return

        val current = progress?.takeIf { it.trackId == trackId }
            ?: loadProgress(trackId)
            ?: return
        if(current.pointCount == 0 && !countdownDone) countdown()

        val next = current.add(point) ?: return //Don't store the same location twice
        addTrackPoint(next)
            .onSuccess { progress = next }
            .onFailure { Log.e(TAG, "onLocation:e: $it") }

        /// Speech every 100 m
        val distance = next.distance.toInt()
        val newSpeech = (distance / 100) / 10f
        if(newSpeech > speechKm) {
            speechKm = newSpeech
            speaker.speak(distance.toDistanceSpeech(this))
            speaker.speak((point.time - next.timeIni).toTimeSpeech(this))
        }
    }

    /// Loads the track once, when tracking starts or the service is recreated
    private suspend fun loadProgress(trackId: Long): TrackProgress? =
        readTrack(trackId).getOrNull()
            ?.let { TrackProgress.from(it) }
            ?.also {
                progress = it
                speechKm = (it.distance.toInt() / 100) / 10f
            }

    private suspend fun countdown() {
        countdownDone = true
        for(i in 5 downTo 1) {
            speaker.speak("$i")
            delay(800)
        }
        speaker.speak(getString(R.string.menu_start))
    }

    private fun stop() {
        trackingJob?.cancel()// Cancelling the collector stops the location updates
        trackingJob = null
        StopTrackingVo2MaxWork.create(applicationContext)
        StopTrackingSpeechWork.create(applicationContext)
        speaker.shutdown()
        isRunning = false
    }

    companion object {
        private const val TAG = "TrackingService"
        private const val NOTIFICATION_ID = 12345678

        private const val A_MINUTE = 60_000L
        private const val MIN_PERIOD = A_MINUTE / 2
        private const val MAX_PERIOD = 10 * A_MINUTE
        private const val MAX_DISTANCE = 1000f

        @Volatile
        var isRunning = false
            private set

        /// Minutes from settings to milliseconds, 0 means as fast as possible (30 s)
        fun intervalMillis(minutes: Int): Long = when {
            minutes < 1 -> MIN_PERIOD
            minutes > 10 -> MAX_PERIOD
            else -> minutes * A_MINUTE
        }

        fun distanceMeters(meters: Int): Float = meters.toFloat().coerceIn(0f, MAX_DISTANCE)
    }
}
