package com.cesoft.cesrunner.ui.home.mvi

sealed class HomeSideEffect {
    data object Close: HomeSideEffect()
}
