package com.cesoft.cesrunner.ui.home.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import kotlinx.coroutines.flow.StateFlow

sealed class HomeState: LoggableState {
    data object Loading: HomeState()
    data class Init(
        val vo2Max: Double,
        val trackFlow: StateFlow<TrackDto?>,
        val locationFlow: StateFlow<TrackPointDto?>? = null,
        val error: AppError? = null
    ): HomeState()
}