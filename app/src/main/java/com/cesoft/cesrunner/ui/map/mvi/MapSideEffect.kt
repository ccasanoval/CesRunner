package com.cesoft.cesrunner.ui.map.mvi

sealed class MapSideEffect {
    data object Close: MapSideEffect()
}
