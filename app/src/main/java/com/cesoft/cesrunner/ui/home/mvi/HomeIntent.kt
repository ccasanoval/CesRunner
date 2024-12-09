package com.cesoft.cesrunner.ui.home.mvi

import com.adidas.mvi.Intent

sealed class HomeIntent: Intent {
    data object Close: HomeIntent()
    data object Load: HomeIntent()
    data object GoStart: HomeIntent()
    data object GoSettings: HomeIntent()
    data object GoMap: HomeIntent()
    data object GoGnss: HomeIntent()
    data object GoTracks: HomeIntent()
}