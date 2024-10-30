package com.cesoft.cesrunner.data

import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import com.cesoft.cesrunner.data.location.LocationDataSource
import com.cesoft.cesrunner.data.prefs.PrefDataSource
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class Repository(
    private val context: Context,
    private val locationDataSource: LocationDataSource,
): RepositoryContract {
    /// PREFS
    override suspend fun readSettings(): Result<SettingsDto> {
        return PrefDataSource(context).readSettings()
    }
    override suspend fun saveSettings(data: SettingsDto): Result<Unit> {
        return PrefDataSource(context).saveSettings(data)
    }

    override suspend fun readCurrentTracking(): Result<CurrentTrackingDto> {
        return PrefDataSource(context).readCurrentTracking()
    }
    override suspend fun saveCurrentTracking(data: CurrentTrackingDto): Result<Unit> {
        return PrefDataSource(context).saveCurrentTracking(data)
    }


    /// TRACKING
    override fun requestLocationUpdates(): Result<MutableStateFlow<Location?>> {
        try {
            val locationFlow = locationDataSource.requestLocationUpdates()
            return Result.success(locationFlow)
        }
        catch(e: Exception) {
            return Result.failure(e)
        }
    }
    override fun stopLocationUpdates(): Result<Unit> {
        try {
            locationDataSource.stopLocationUpdates()
            return Result.success(Unit)
        }
        catch(e: Exception) {
            return Result.failure(e)
        }
    }
}