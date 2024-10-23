package com.cesoft.cesrunner.ui.tracks.mvi

import com.adidas.mvi.LoggableState

sealed class TracksState: LoggableState {
    data object Init: TracksState()
}