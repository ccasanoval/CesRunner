package com.cesoft.cesrunner.ui.settings.mvi

import com.adidas.mvi.transform.ViewTransform

internal object SettingsTransform {
    data class Load(val state: SettingsState.Init): ViewTransform<SettingsState, SettingsSideEffect>() {
        override fun mutate(currentState: SettingsState): SettingsState {
            return state
        }
    }
}
