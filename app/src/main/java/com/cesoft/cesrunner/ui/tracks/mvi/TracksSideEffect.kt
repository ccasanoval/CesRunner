package com.cesoft.cesrunner.ui.tracks.mvi

sealed class TracksSideEffect {
    data object Close: TracksSideEffect()
}
