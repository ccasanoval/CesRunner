package com.cesoft.cesrunner.ui.tracking.mvi

sealed class TrackingSideEffect {
    data object Close: TrackingSideEffect()
}
