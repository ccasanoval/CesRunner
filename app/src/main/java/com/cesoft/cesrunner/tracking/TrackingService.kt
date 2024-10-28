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
import androidx.lifecycle.lifecycleScope
import com.cesoft.cesrunner.data.prefs.readBool
import com.cesoft.cesrunner.data.prefs.writeBool
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class TrackingService : LifecycleService() {
    //Checks whether the bound activity has really gone away
    // (foreground service with notification created) or simply orientation change (no-op).
    private var configurationChange = false
    private var serviceRunningInForeground = false
    private val localBinder = LocalBinder()
    private var currentLocation: Location? = null
    private var locationFlow: Job? = null
    private lateinit var notificationManager: NotificationManager

//    @Inject lateinit var getLocations: GetLocationsUseCase
//    @Inject lateinit var getSession: GetSessionUseCase
//    @Inject lateinit var readLogin: ReadLoginUseCase
//    @Inject lateinit var sendTracking: SendTrackingUseCase
//    @Inject lateinit var sendTrackingBatch: SendTrackingBatchUseCase
//    @Inject lateinit var saveTrackingItem: SaveTrackingItemUseCase
//    @Inject lateinit var listTrackingItems: ListTrackingItemsUseCase
//    @Inject lateinit var clearTrackingItems: ClearTrackingItemsUseCase

    override fun onCreate() {
        super.onCreate()
        TrackingNotification.CANCEL_TRACKING = "$packageName.CANCEL_TRACKING"
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val cancelLocationTrackingFromNotification =
            intent?.getBooleanExtra(TrackingNotification.CANCEL_TRACKING, false)
        if(cancelLocationTrackingFromNotification == true) {
            unsubscribeToLocationUpdates()
            stopSelf()
        }

        //NOTE: !!!
        // Must call startForeground() within 10 seconds of receiving onStartCommand()
        // after starting your service using startForegroundService()
        //https://medium.com/@domen.lanisnik/guide-to-foreground-services-on-android-9d0127dc8f9a
        val notification = TrackingNotification.generateNotification(
            this@TrackingService, notificationManager)
        startForeground(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_LOCATION)

        // Tells the system not to recreate the service after it's been killed.
        return super.onStartCommand(intent, flags, START_NOT_STICKY)
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        // MainActivity comes into foreground and binds to service
        stopForeground(STOP_FOREGROUND_REMOVE)//STOP_FOREGROUND_LEGACY
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

    override fun onRebind(intent: Intent) {
        // MainActivity (client) returns to the foreground and rebinds to service,
        // so the service can become a background services.
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceRunningInForeground = false
        configurationChange = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
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

    override fun onDestroy() {
        Log.e(TAG, "--------onDestroy()")
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        //Log.e(TAG, "--------onConfigurationChanged()")
        super.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    fun subscribeToLocationUpdates() {
        //Log.e(TAG, "--------subscribeToLocationUpdates()")
        lifecycleScope.launch { writeBool(PREF_TRACKING_STATUS, true) }

        // Binding to this service doesn't actually trigger onStartCommand(). That is needed to
        // ensure this Service can be promoted to a foreground service,
        // i.e., the service needs to be officially started (which we do here).
        try {
            startService(Intent(applicationContext, TrackingService::class.java))
        } catch(_: Exception) { }

        // Observe locations via Flow as they are generated by the repository
        /*
        locationFlow = getLocations()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { location ->
                currentLocation = location
                Log.e(TAG, "----------- Service location: ${location.toText()} ------------")
                delay(100)
                val res = getSession()
                if(res.isSuccess) {
                    // Check if there are locations in db to send to backend
                    val list = listTrackingItems().getOrNull() ?: listOf()
                    if(list.isNotEmpty()) {
                        Log.e(TAG, "---- Sending stored trackings ${list.size} ----")
                        if(sendTrackingBatch(list).isSuccess) {
                            clearTrackingItems()
                        }
                    }
                    res.getOrNull()?.let {
                        if(it.isActive && !it.isPaused) {
                            if(sendTracking(location).isFailure) {
                                saveTrackingItem(location)
                            }
                        }
                    }
                }
                else {
                    // If no connection, you can't ask for session... but still send the tracking
                    if(!isInetAvailable(this)) {
                        Log.e(TAG, "---- No connection - storing location ----")
                        saveTrackingItem(location)
                    }
                }
            }
            .launchIn(lifecycleScope)*/
    }

    fun unsubscribeToLocationUpdates() {
        Log.e(TAG, "-------------unsubscribeToLocationUpdates()")
        locationFlow?.cancel()
        lifecycleScope.launch {
            writeBool(PREF_TRACKING_STATUS, false)
        }
    }

    inner class LocalBinder : Binder() {
        internal val service: TrackingService
            get() = this@TrackingService
    }

    companion object {
        private const val TAG = "TrackingService"
        private const val NOTIFICATION_ID = 12345678
        private const val PREF_TRACKING_STATUS = "PREF_TRACKING_STATUS"
    }
}
