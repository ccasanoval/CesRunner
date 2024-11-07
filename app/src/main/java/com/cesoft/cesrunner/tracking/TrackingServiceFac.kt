package com.cesoft.cesrunner.tracking

import android.content.Context
import android.content.Intent

class TrackingServiceFac(private val context: Context) {
    fun start(minInterval: Int, minDistance: Float) {
        TrackingService.period = minInterval.toLong()
        TrackingService.distance = minDistance
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