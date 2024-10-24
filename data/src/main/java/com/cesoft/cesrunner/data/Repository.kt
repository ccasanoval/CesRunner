package com.cesoft.cesrunner.data

import android.content.Context
import com.cesoft.cesrunner.data.prefs.PrefDataSource
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract

class Repository(
    private val context: Context
): RepositoryContract {
    override suspend fun readSettings(): Result<SettingsDto> {
        return PrefDataSource(context).readSettings()
    }

    override suspend fun saveSettings(settings: SettingsDto): Result<Unit> {
        return PrefDataSource(context).saveSettings(settings)
    }
}