package com.cesoft.cesrunner.ui.settings.mvi

import com.adidas.mvi.Intent

sealed class SettingsIntent: Intent {
    data object Close: SettingsIntent()
}