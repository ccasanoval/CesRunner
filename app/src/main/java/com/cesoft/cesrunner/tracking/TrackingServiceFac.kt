package com.cesoft.cesrunner.tracking

import android.content.Context
import android.content.Intent

class TrackingServiceFac(private val context: Context) {
    fun start() {
        if(TrackingService.isRunning) return
        // The service calls startForeground() as soon as it starts
        context.startForegroundService(Intent(context, TrackingService::class.java))
    }
    fun stop() {
        context.stopService(Intent(context, TrackingService::class.java))
    }
}
