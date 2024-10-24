package com.cesoft.cesrunner.ui.settings.mvi

import com.adidas.mvi.LoggableState
import com.cesoft.cesrunner.domain.entity.SettingsDto

sealed class SettingsState: LoggableState {
    data object Loading: SettingsState()
    data class Init(
        val settings: SettingsDto,
    ): SettingsState()
    //public data class LoggedIn(val username: String) : HomeState()
}