package com.cesoft.cesrunner.ui.gnss.mvi

import com.adidas.mvi.Intent

sealed class GnssIntent: Intent {
    data object Close: GnssIntent()
    data object Load: GnssIntent()
}