package com.cesoft.cesrunner.ui.tracking.mvi

import com.adidas.mvi.Intent

sealed class TrackingIntent: Intent {
    data object Close: TrackingIntent()
    data object Load: TrackingIntent()
}