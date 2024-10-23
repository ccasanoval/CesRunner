package com.cesoft.cesrunner.ui.settings.mvi

import com.adidas.mvi.LoggableState

sealed class SettingsState: LoggableState {
    data object Init: SettingsState()
    //public data class LoggedIn(val username: String) : HomeState()
}