package com.cesoft.cesrunner.ui.tracks.mvi

sealed class TracksSideEffect {
    data object Close: TracksSideEffect()
    data class Details(val id: Long): TracksSideEffect()
}
