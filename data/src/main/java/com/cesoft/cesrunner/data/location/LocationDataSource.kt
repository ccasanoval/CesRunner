package com.cesoft.cesrunner.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.flow.MutableStateFlow

@SuppressLint("MissingPermission")
class LocationDataSource(
    context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    init {
        android.util.Log.e(TAG, "INIT:---------- 00000")
        locationManager.getCurrentLocation(
            LocationManager.NETWORK_PROVIDER,
            null,
            context.applicationContext.mainExecutor
        ) {
            android.util.Log.e(TAG, "INIT:NET:---------- $it")
            _currentLocation = it
        }
        locationManager.getCurrentLocation(
            LocationManager.GPS_PROVIDER,
            null,
            context.applicationContext.mainExecutor
        ) {
            android.util.Log.e(TAG, "INIT:GPS:---------- $it")
            _currentLocation = it
        }
    }

    // Cached location in the system: doesn't activate GPS chip
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): Location? {
        val net = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if(gps != null) return gps
        if(net != null) return net
        return _currentLocation
    }

    private var _currentLocation: Location? = null
    private val _locationFlow: MutableStateFlow<Location?> = MutableStateFlow(null)
    private val _locationListener: LocationListener = object : LocationListener {
        @SuppressLint("MissingPermission")
        override fun onLocationChanged(location: Location) {
            _currentLocation = location
            _locationFlow.tryEmit(location)
            android.util.Log.e(TAG, "*** onLocationChanged: $location")
        }
        override fun onProviderEnabled(provider: String) {
            android.util.Log.e(TAG, "*** locationListener: $provider")
        }
        override fun onProviderDisabled(provider: String) {
            android.util.Log.e(TAG, "*** onProviderDisabled: $provider")
        }
    }
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(minInterval: Long, minDistance: Float): MutableStateFlow<Location?> {
        // FUSED_PROVIDER, ==> Google doesn't want FUSED to work!!
        //https://stackoverflow.com/questions/35456254/application-getting-wrong-location-until-open-inbuilt-google-map-application
        //TODO: Add network provider?
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            minInterval / 2,
            minDistance,
            _locationListener)
        return _locationFlow
    }
    fun stopLocationUpdates() {
        locationManager.removeUpdates(_locationListener)
    }

//    fun isGpsOn(): Boolean = locationManager.isLocationEnabled
//    fun checkGpsStateAndStart() {
//        if( ! locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            context.startActivity(intent)
//        }
//    }

    companion object {
        private const val TAG = "LocationDS"
    }
}