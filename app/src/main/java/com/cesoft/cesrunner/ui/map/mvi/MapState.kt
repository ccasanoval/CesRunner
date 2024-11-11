package com.cesoft.cesrunner.ui.map.mvi

import com.adidas.mvi.LoggableState

sealed class MapState: LoggableState {
    data object Init: MapState()
}