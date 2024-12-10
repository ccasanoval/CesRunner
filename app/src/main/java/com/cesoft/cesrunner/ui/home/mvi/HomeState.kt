package com.cesoft.cesrunner.ui.home.mvi

import android.location.Location
import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import kotlinx.coroutines.flow.StateFlow

sealed class HomeState: LoggableState {
    data object Loading: HomeState()
    data class Init(
        val trackFlow: StateFlow<TrackDto?>,
        val locationFlow: StateFlow<Location?>? = null,
        val error: AppError? = null
    ): HomeState()
}