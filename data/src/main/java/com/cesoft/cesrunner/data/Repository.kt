package com.cesoft.cesrunner.data

import android.content.Context
import android.location.Location
import com.cesoft.cesrunner.data.local.AppDatabase
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
import com.cesoft.cesrunner.data.location.LocationDataSource
import com.cesoft.cesrunner.data.prefs.PrefDataSource
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract
import kotlinx.coroutines.flow.MutableStateFlow

class Repository(
    private val context: Context,
    private val locationDataSource: LocationDataSource,
    private val db: AppDatabase,
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

    /// TRACKING SERVICE
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

    /// TRACKING DATABASE
    override suspend fun createTrack(data: TrackDto): Result<Unit> {
        try {
            db.trackDao().create(LocalTrackDto.fromModel(data))
            return Result.success(Unit)
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }
    override suspend fun addTrackPoint(id: Long, data: TrackPointDto): Result<Unit> {
        try {
            db.trackPointDao().add(LocalTrackPointDto.fromModel(id, data))
            return Result.success(Unit)
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }
    override suspend fun readTrack(id: Long): Result<TrackDto> {
        try {
            val points = db.trackPointDao().readByTrackId(id)
            val track = db.trackDao().getById(id)
            return Result.success(track.toModel(points))
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }
    override suspend fun readAllTrack(): Result<List<TrackDto>> {
        return Result.success(listOf())
    }
    override suspend fun deleteTrack(id: Long): Result<Unit> {
        return Result.success(Unit)
    }

}