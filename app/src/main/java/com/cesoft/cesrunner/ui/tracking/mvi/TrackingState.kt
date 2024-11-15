package com.cesoft.cesrunner.ui.tracking.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import kotlinx.coroutines.flow.StateFlow

sealed class TrackingState: LoggableState {
    data object Loading: TrackingState()
    data class Init(
        val trackFlow: StateFlow<TrackDto?>,
        val error: AppError? = null
    ): TrackingState()
}