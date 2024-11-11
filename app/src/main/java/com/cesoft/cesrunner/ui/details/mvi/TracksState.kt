package com.cesoft.cesrunner.ui.details.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto

sealed class TrackDetailsState: LoggableState {
    data object Loading: TrackDetailsState()
    data class Init(
        val tracks: List<TrackDto>,//Check efficiency of compose recomposition: ImmutableList?
        val error: AppError? = null
    ): TrackDetailsState()
}