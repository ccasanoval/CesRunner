package com.cesoft.cesrunner.ui.tracks.mvi

import com.adidas.mvi.Intent

sealed class TracksIntent : Intent {
    data object Load: TracksIntent()
    data object Close: TracksIntent()
    data class Details(val id: Long): TracksIntent()
}