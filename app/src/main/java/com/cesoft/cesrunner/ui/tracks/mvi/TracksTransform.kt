package com.cesoft.cesrunner.ui.tracks.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingSideEffect
import com.cesoft.cesrunner.ui.tracking.mvi.TrackingState
import kotlinx.coroutines.flow.Flow

internal object TracksTransform {
    data class AddSideEffect(
        val sideEffect: TracksSideEffect
    ) : SideEffectTransform<TracksState, TracksSideEffect>() {
        override fun mutate(sideEffects: SideEffects<TracksSideEffect>): SideEffects<TracksSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }

    data class GoInit(
        val data: List<TrackDto>,
        val error: AppError? = null,
    ) : ViewTransform<TracksState, TracksSideEffect>() {
        override fun mutate(currentState: TracksState): TracksState {
            return TracksState.Init(data, error)
        }
    }
}