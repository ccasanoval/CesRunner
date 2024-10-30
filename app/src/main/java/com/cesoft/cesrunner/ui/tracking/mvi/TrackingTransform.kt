package com.cesoft.cesrunner.ui.tracking.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto

internal object TrackingTransform {

    data class AddSideEffect(
        val sideEffect: TrackingSideEffect
    ): SideEffectTransform<TrackingState, TrackingSideEffect>() {
        override fun mutate(sideEffects: SideEffects<TrackingSideEffect>): SideEffects<TrackingSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }

    data class GoInit(
        val data: CurrentTrackingDto,
        val error: AppError?,
    ): ViewTransform<TrackingState, TrackingSideEffect>() {
        override fun mutate(currentState: TrackingState): TrackingState {
            return TrackingState.Init(data, error)
        }
    }
}