package com.cesoft.cesrunner.ui.gnss.mvi

sealed class GnssSideEffect {
    data object Close: GnssSideEffect()
}
