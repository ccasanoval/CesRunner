package com.cesoft.cesrunner.tracking

import android.content.Context
import android.content.Intent
import android.util.Log
import org.koin.core.annotation.Single

@Single
class TrackingServiceFac(private val context: Context) {
    private var started = false
    fun start(minInterval: Int, minDistance: Int) {
        Log.e("TrackingServiceFac", "---------- START ----------- started = $started // service = "+TrackingService.isRunning)
        if(started) return
        started = true
        if( ! TrackingService.isRunning) {
            TrackingService.period = minInterval.toLong()
            TrackingService.distance = minDistance.toFloat()
            val intent = Intent(context, TrackingService::class.java)
            context.startService(intent)
        }
    }
    fun stop() {
        Log.e("TrackingServiceFac", "---------- STOP ----------- started = $started // service = "+TrackingService.isRunning)
        started = false
        val intent = Intent(context, TrackingService::class.java)
        context.stopService(intent)
    }
}