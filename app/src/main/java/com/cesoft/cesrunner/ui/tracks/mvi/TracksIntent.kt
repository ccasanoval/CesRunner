package com.cesoft.cesrunner.ui.tracks.mvi

import com.adidas.mvi.Intent

sealed class TracksIntent : Intent {
    data object Close: TracksIntent()
}