package com.cesoft.cesrunner.ui.details.mvi

sealed class TrackDetailsSideEffect {
    data object Close: TrackDetailsSideEffect()
}
