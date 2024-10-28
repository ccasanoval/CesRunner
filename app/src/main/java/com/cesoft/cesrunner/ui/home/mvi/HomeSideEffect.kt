package com.cesoft.cesrunner.ui.home.mvi

sealed class HomeSideEffect {
    data object Start: HomeSideEffect()
    data object GoSettings: HomeSideEffect()
    data object GoTracks: HomeSideEffect()
    data object GoMaps: HomeSideEffect()
    data object Close: HomeSideEffect()
}
