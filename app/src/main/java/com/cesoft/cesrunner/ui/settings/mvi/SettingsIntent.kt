package com.cesoft.cesrunner.ui.settings.mvi

import com.adidas.mvi.Intent
import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.entity.SettingsDto

sealed class SettingsIntent: Intent {
    data object Load: SettingsIntent()
    data class Save(val settings: SettingsDto): SettingsIntent()
    data class SaveApiKeys(val apiKeys: ApiKeysDto): SettingsIntent()
}
