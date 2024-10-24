package com.cesoft.cesrunner.domain.repository

import com.cesoft.cesrunner.domain.entity.SettingsDto

interface RepositoryContract {
    suspend fun readSettings(): Result<SettingsDto>
    suspend fun saveSettings(settings: SettingsDto): Result<Unit>
}