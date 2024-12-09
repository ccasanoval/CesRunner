package com.cesoft.cesrunner.ui.gnss.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError

sealed class GnssState: LoggableState {
    data object Loading: GnssState()
    data class Init(
        val error: AppError? = null
    ): GnssState()
}