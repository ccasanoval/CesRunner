package com.cesoft.cesrunner.tracking

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.cesoft.cesrunner.data.prefs.readBool
import com.cesoft.cesrunner.data.prefs.writeBool
import com.cesoft.cesrunner.domain.usecase.ReadCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.RequestLocationUpdatesUC
import com.cesoft.cesrunner.domain.usecase.SaveCurrentTrackingUC
import com.cesoft.cesrunner.domain.usecase.StopLocationUpdatesUC
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class TrackingService: LifecycleService() {
    //Checks whether the bound activity has really gone away
    // (foreground service with notification created) or simply orientation change (no-op).
    private var configurationChange = false
    private var serviceRunningInForeground = false

    private var locationFlow: Job? = null
    private lateinit var notificationManager: NotificationManager

    private val readCurrentTracking: ReadCurrentTrackingUC by inject()
    private val saveCurrentTracking: SaveCurrentTrackingUC by inject()
    private val requestLocationUpdates: RequestLocationUpdatesUC by inject()
    private val stopLocationUpdates: StopLocationUpdatesUC by inject()

    override fun onCreate() {
        Log.e(TAG, "onCreate----------------------------------")
        super.onCreate()
        _isRunning = true
        TrackingNotification.CANCEL_TRACKING = "$packageName.CANCEL_TRACKING"
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy----------------------------------")
        _isRunning = false
        stop()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand---------------------------------- $isRunning")

        val cancelLocationTrackingFromNotification =
            intent?.getBooleanExtra(TrackingNotification.CANCEL_TRACKING, false)
        if(cancelLocationTrackingFromNotification == true) {
            stop()
            stopSelf()
        }

        //NOTE: !!!
        // Must call startForeground() within 10 seconds of receiving onStartCommand()
        // after starting your service using startForegroundService()
        //https://medium.com/@domen.lanisnik/guide-to-foreground-services-on-android-9d0127dc8f9a
        val notification = TrackingNotification.generateNotification(
            this@TrackingService, notificationManager)
        startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)

        start()
        // Tells the system not to recreate the service after it's been killed.
        //return super.onStartCommand(intent, flags, START_NOT_STICKY)
        return super.onStartCommand(intent, flags, START_STICKY)
    }

    private fun start() {
        Log.e(TAG, "start----------------------------------")
        requestLocationUpdates().getOrNull()
            ?.onEach { location ->
                location?.let {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    val instant = Instant.ofEpochMilli(it.time)
                    val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                    val dateStr = formatter.format(date)
                    val data = "POS: ${it.latitude}, ${it.longitude}\n"+
                            "TIME: ${dateStr}\n"+
                            "PROV: ${it.provider}\n"+
                            "ACCU: ${it.accuracy}\n"+
                            "ALT:  ${it.altitude}\n"+
                            "ORI:  ${it.bearing}\n"+
                            "SPE:  ${it.speed}\n"+
                            "MOCK: ${it.isMock}\n"
                            //"EXT:  ${it.extras}"
                    //it.isComplete, it.mslAltitudeMeters
                    Log.e(TAG, "----------- Service location:\n$data ------------")
                }
                Log.e(TAG, "----------- Service location: $location ------------")
                delay(100)
            }
            ?.launchIn(lifecycleScope)
    }

    private fun stop() {
        Log.e(TAG, "stop----------------------------------")
        locationFlow?.cancel()
        stopLocationUpdates()
        lifecycleScope.launch {
            writeBool(PREF_TRACKING_STATUS, false)
        }
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.e(TAG, "onConfigurationChanged---------------------------------$newConfig")
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.e(TAG, "onTaskRemoved----------------------------------")
    }

    /*
    private val localBinder = LocalBinder()
    override fun onBind(intent: Intent): IBinder {
        Log.e(TAG, "onBind----------------------------------")
        super.onBind(intent)
        // MainActivity comes into foreground and binds to service
        stopForeground(STOP_FOREGROUND_REMOVE)//STOP_FOREGROUND_LEGACY
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        android.util.Log.e(TAG, "onRebind----------------------------------")
        // MainActivity (client) returns to the foreground and rebinds to service,
        // so the service can become a background services.
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        android.util.Log.e(TAG, "onUnbind----------------------------------")
        lifecycleScope.launch {
            val tracking = readBool(PREF_TRACKING_STATUS)
            Log.e(TAG, "----onUnbind  (configChange=$configurationChange / tracking=$tracking)")
            // MainActivity leaves foreground, so service needs to become a foreground service
            // If method is called due to configuration change in MainActivity, we do nothing.
            if(!configurationChange && tracking) {
                Log.e(TAG, "----onUnbind: Start foreground service")
                val notification = TrackingNotification.generateNotification(
                    this@TrackingService, notificationManager)
                try {
                    startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)
                    serviceRunningInForeground = true
                }
                catch(e: Exception) {
                    Log.e(TAG, "onUnbind: foreground service exception: $e")
                }
            }
        }
        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }
    inner class LocalBinder: Binder() {
        internal val service: TrackingService
            get() = this@TrackingService
    }*/

    companion object {
        private const val TAG = "TrackingService"
        private const val NOTIFICATION_ID = 12345678
        private const val PREF_TRACKING_STATUS = "PREF_TRACKING_STATUS"
        private var _isRunning = false
        val isRunning: Boolean
            get() = _isRunning
    }
}
