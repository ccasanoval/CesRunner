package com.cesoft.cesrunner.ui.map.mvi

import com.adidas.mvi.Intent

sealed class MapIntent : Intent {
    data object Close: MapIntent()
}