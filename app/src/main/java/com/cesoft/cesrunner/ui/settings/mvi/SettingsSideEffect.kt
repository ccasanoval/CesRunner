package com.cesoft.cesrunner.ui.settings.mvi

sealed class SettingsSideEffect {
    data object Close: SettingsSideEffect()
}
