package com.cesoft.cesrunner.ui.home.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import kotlinx.coroutines.flow.Flow

sealed class HomeState: LoggableState {
    data object Loading: HomeState()
    data class Init(
        val trackFlow: Flow<TrackDto?>,
        val error: AppError? = null
    ): HomeState()
}