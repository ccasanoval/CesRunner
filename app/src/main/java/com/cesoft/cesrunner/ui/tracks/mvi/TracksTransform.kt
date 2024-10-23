package com.cesoft.cesrunner.ui.tracks.mvi

import com.adidas.mvi.sideeffects.SideEffects
import com.adidas.mvi.transform.SideEffectTransform
import com.adidas.mvi.transform.ViewTransform

internal object TracksTransform {
    object Close: ViewTransform<TracksState, TracksSideEffect>() {
        override fun mutate(currentState: TracksState): TracksState {
            return TracksState.Init
        }
    }
}