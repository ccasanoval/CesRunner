package com.cesoft.cesrunner.tracking

import android.content.Context
import android.content.Intent

class TrackingServiceFac(private val context: Context) {
    fun start(period: Int, distance: Int) {
        TrackingService.period = period.toLong()
        TrackingService.distance = distance
        android.util.Log.e("TrackServFac", "start --------- period = $period / distance = $distance")
        if( ! TrackingService.isRunning) {
            val intent = Intent(context, TrackingService::class.java)
            context.startService(intent)
        }
    }
    fun stop() {
        val intent = Intent(context, TrackingService::class.java)
        context.stopService(intent)
    }
}