package com.cesoft.cesrunner.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow

class LocationDataSource(
    private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    // Cached location in the system: doesn't activate GPS chip
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): Location? {
        return locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
    }

    private var _currentLocation: Location? = null
    private val _locationFlow: MutableStateFlow<Location?> = MutableStateFlow(null)
    private val _locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _currentLocation = location
            _locationFlow.tryEmit(location)
            android.util.Log.e(TAG, "onLocationChanged: $_currentLocation ----------------")
        }
        override fun onProviderEnabled(provider: String) {
            android.util.Log.e(TAG, "locationListener: $provider ----------------")
        }
        override fun onProviderDisabled(provider: String) {
            android.util.Log.e(TAG, "onProviderDisabled: $provider --------------")
        }
    }
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(): MutableStateFlow<Location?> {
        val minInterval = 30*1000L//millis
        val minDistance = 0f//5f//m
        locationManager.requestLocationUpdates(
            LocationManager.FUSED_PROVIDER,
            minInterval,
            minDistance,
            _locationListener)
        return _locationFlow
    }
    fun stopLocationUpdates() {
        locationManager.removeUpdates(_locationListener)
    }

    fun isGpsOn(): Boolean =locationManager.isLocationEnabled
    fun checkGpsStateAndStart() {
        if( ! locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }
    }

    companion object {
        private const val TAG = "LocationDS"
    }
}