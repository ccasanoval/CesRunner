package com.cesoft.cesrunner.ui.tracking.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto

sealed class TrackingState: LoggableState {
    data object Loading: TrackingState()
    data class Init(
        val currentTracking: CurrentTrackingDto,
        val error: AppError? = null
    ): TrackingState()
}