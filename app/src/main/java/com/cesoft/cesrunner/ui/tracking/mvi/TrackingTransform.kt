package com.cesoft.cesrunner.ui.tracking.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform

internal object TrackingTransform {

    data class AddSideEffect(
        val sideEffect: TrackingSideEffect
    ): SideEffectTransform<TrackingState, TrackingSideEffect>() {
        override fun mutate(sideEffects: SideEffects<TrackingSideEffect>): SideEffects<TrackingSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }
}