package com.cesoft.cesrunner.ui.home.mvi

import com.adidas.mvi.LoggableState

sealed class HomeState: LoggableState {
    data object Init: HomeState()
    //public data class LoggedIn(val username: String) : HomeState()
}