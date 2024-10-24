package com.cesoft.cesrunner.domain.usecase

import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract

class SaveSettingsUC(private val repository: RepositoryContract) {
    suspend operator fun invoke(settings: SettingsDto) = repository.saveSettings(settings)
}
