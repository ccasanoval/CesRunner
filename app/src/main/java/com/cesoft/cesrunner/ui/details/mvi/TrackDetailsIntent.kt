package com.cesoft.cesrunner.ui.details.mvi

import com.adidas.mvi.Intent

sealed class TrackDetailsIntent : Intent {
    data object Load: TrackDetailsIntent()
    data object Close: TrackDetailsIntent()
    data object Delete: TrackDetailsIntent()
    data object Export: TrackDetailsIntent()
    data class SaveName(val name: String): TrackDetailsIntent()
}