package com.cesoft.cesrunner.ui.home.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto

sealed class HomeState: LoggableState {
    data object Loading: HomeState()
    data class Init(
        val currentTracking: CurrentTrackingDto,
        val error: AppError? = null
    ): HomeState()
}