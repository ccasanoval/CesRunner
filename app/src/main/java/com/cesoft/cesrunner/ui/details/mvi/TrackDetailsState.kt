package com.cesoft.cesrunner.ui.details.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.MessageType
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.LocationDto
import com.cesoft.cesrunner.domain.entity.TrackDto

sealed class TrackDetailsState: LoggableState {
    data object Loading: TrackDetailsState()
    data class Init(
        val track: TrackDto,
        val location: LocationDto? = null,
        val error: AppError? = null,
        val message: MessageType? = null,
    ): TrackDetailsState()
}