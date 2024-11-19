package com.cesoft.cesrunner.ui.map.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform

internal object MapTransform {

    data class AddSideEffect(
        val sideEffect: MapSideEffect
    ): SideEffectTransform<MapState, MapSideEffect>() {
        override fun mutate(sideEffects: SideEffects<MapSideEffect>): SideEffects<MapSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }

    data class Load(val state: MapState.Init): ViewTransform<MapState, MapSideEffect>() {
        override fun mutate(currentState: MapState): MapState {
            return state
        }
    }
}