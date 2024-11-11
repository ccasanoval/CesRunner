package com.cesoft.cesrunner.ui.details.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.TrackDto

internal object TrackDetailsTransform {
    data class AddSideEffect(
        val sideEffect: TrackDetailsSideEffect
    ) : SideEffectTransform<TrackDetailsState, TrackDetailsSideEffect>() {
        override fun mutate(sideEffects: SideEffects<TrackDetailsSideEffect>): SideEffects<TrackDetailsSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }

    data class GoInit(
        val data: TrackDto,
        val error: AppError? = null,
    ) : ViewTransform<TrackDetailsState, TrackDetailsSideEffect>() {
        override fun mutate(currentState: TrackDetailsState): TrackDetailsState {
            return TrackDetailsState.Init(data, error)
        }
    }
}