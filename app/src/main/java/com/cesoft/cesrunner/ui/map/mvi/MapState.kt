package com.cesoft.cesrunner.ui.map.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.LocationDto
import com.cesoft.cesrunner.domain.entity.TrackDto

sealed class MapState: LoggableState {
    data object Loading: MapState()
    data class Init(
        val track: TrackDto = TrackDto.Empty,
        val location: LocationDto?,
        val error: AppError? = null,
    ): MapState()
}