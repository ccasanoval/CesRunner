package com.cesoft.cesrunner.ui.details.mvi

import com.adidas.mvi.Intent

sealed class TrackDetailsIntent : Intent {
    data object Load: TrackDetailsIntent()
    data object Close: TrackDetailsIntent()
}