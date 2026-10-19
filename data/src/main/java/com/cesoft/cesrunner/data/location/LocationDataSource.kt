package com.cesoft.cesrunner.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission")
class LocationDataSource(
    private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Last location received by any consumer, used as fallback when the system has none cached
    @Volatile
    private var currentLocation: Location? = null

    init {
        // Warm up the cache, so the first screens can show something before any update arrives
        for(provider in listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)) {
            try {
                locationManager.getCurrentLocation(provider, null, context.mainExecutor) { location ->
                    location?.let { currentLocation = it }
                }
            }
            catch(e: Exception) {
                Log.e(TAG, "init: $provider: $e")
            }
        }
    }

    // Cached location in the system: doesn't activate GPS chip
    fun getLastKnownLocation(): Location? =
        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: currentLocation

    /// Each collector gets its own listener, so different consumers (home screen, tracking service)
    /// don't overwrite each other's interval, and updates stop when the collector is cancelled.
    fun requestLocationUpdates(minInterval: Long, minDistance: Float): Flow<Location> = callbackFlow {
        val listener = LocationListener { location ->
            currentLocation = location
            trySend(location)
        }
        // FUSED_PROVIDER, ==> Google doesn't want FUSED to work!!
        //https://stackoverflow.com/questions/35456254/application-getting-wrong-location-until-open-inbuilt-google-map-application
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            minInterval / 2,
            minDistance,
            context.mainExecutor,
            listener
        )
        awaitClose { locationManager.removeUpdates(listener) }
    }

    companion object {
        private const val TAG = "LocationDS"
    }
}
