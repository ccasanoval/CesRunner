package com.cesoft.cesrunner.domain.repository

import android.location.Location
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import kotlinx.coroutines.flow.MutableStateFlow

interface RepositoryContract {
    // PREFS
    suspend fun readSettings(): Result<SettingsDto>
    suspend fun saveSettings(data: SettingsDto): Result<Unit>
    suspend fun readCurrentTracking(): Result<CurrentTrackingDto>
    suspend fun saveCurrentTracking(data: CurrentTrackingDto): Result<Unit>

    // TRACKING
    suspend fun requestLocationUpdates(): Result<MutableStateFlow<Location?>>
    suspend fun stopLocationUpdates(): Result<Unit>
}