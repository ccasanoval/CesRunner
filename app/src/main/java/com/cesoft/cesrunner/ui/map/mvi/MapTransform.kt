package com.cesoft.cesrunner.ui.map.mvi

import com.adidas.mvi.transform.ViewTransform

internal object MapTransform {
    object Close: ViewTransform<MapState, MapSideEffect>() {
        override fun mutate(currentState: MapState): MapState {
            return MapState.Init
        }
    }
}