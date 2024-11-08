package com.cesoft.cesrunner.ui.tracking.mvi

import com.adidas.mvi.Intent

sealed class TrackingIntent: Intent {
    data object Load: TrackingIntent()
    data object Close: TrackingIntent()
    //data object Refresh: TrackingIntent()

    data object Pause: TrackingIntent()
    data object Continue: TrackingIntent()
    data object Stop: TrackingIntent()
}