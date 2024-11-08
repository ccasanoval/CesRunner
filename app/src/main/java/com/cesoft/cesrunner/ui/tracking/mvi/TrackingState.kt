package com.cesoft.cesrunner.ui.tracking.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import kotlinx.coroutines.flow.Flow

sealed class TrackingState: LoggableState {
    data object Loading: TrackingState()
    data class Init(
        val trackFlow: Flow<TrackDto>,
        //val currentTracking: TrackDto,
        //val pointFlow: Flow<TrackPointDto>,
        val error: AppError? = null
    ): TrackingState()
}