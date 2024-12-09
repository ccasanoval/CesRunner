package com.cesoft.cesrunner.ui.gnss.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform

internal object GnssTransform {

    data class GoInit(val state: GnssState.Init) : ViewTransform<GnssState, GnssSideEffect>() {
        override fun mutate(currentState: GnssState): GnssState {
            return state
        }
    }

    data class AddSideEffect(
        val sideEffect: GnssSideEffect
    ): SideEffectTransform<GnssState, GnssSideEffect>() {
        override fun mutate(sideEffects: SideEffects<GnssSideEffect>): SideEffects<GnssSideEffect> {
            return sideEffects.add(sideEffect)
        }
    }
}
