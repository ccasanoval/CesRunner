package com.cesoft.cesrunner.ui.tracks.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto

sealed class TracksState: LoggableState {
    data object Loading: TracksState()
    data class Init(
        val tracks: List<TrackDto>,//Check efficiency of compose recomposition: ImmutableList?
        val error: AppError? = null
    ): TracksState()
}