package com.cesoft.cesrunner.data

import android.content.Context
import android.location.Location
import com.cesoft.cesrunner.data.local.AppDatabase
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
import com.cesoft.cesrunner.data.location.LocationDataSource
import com.cesoft.cesrunner.data.prefs.PrefDataSource
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.Common.ID_NULL
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

    override suspend fun readCurrentTrack(): Result<TrackDto> {
        val id = PrefDataSource(context).readCurrentTrackingId(ID_NULL)
        return if(id > ID_NULL) readTrack(id) else Result.failure(AppError.NotFound)
    }
    override suspend fun saveCurrentTrack(id: Long): Result<Unit> {
        PrefDataSource(context).saveCurrentTrackingId(id)
        return Result.success(Unit)
    }

    /// TRACKING SERVICE
    override fun requestLocationUpdates(minInterval: Long, minDistance: Float):
            Result<MutableStateFlow<Location?>> {
        try {
            val locationFlow = locationDataSource.requestLocationUpdates(minInterval, minDistance)
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
    override suspend fun createTrack(data: TrackDto): Result<Long> {
        try {
            val id: Long = db.trackDao().create(LocalTrackDto.fromModel(data))
            android.util.Log.e(TAG, "createTrack ********* ------------  id = $id")
            return if( id > 0) Result.success(id)
            else Result.failure(Throwable())
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }
    override suspend fun updateTrack(data: TrackDto): Result<Unit> {
        try {
            db.trackDao().update(LocalTrackDto.fromModel(data))
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
            val points = db.trackPointDao().getByTrackId(id)
            val track = db.trackDao().getById(id)
            return Result.success(track.toModel(points))
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }
    override suspend fun readLastTrack(): Result<TrackDto> {
        try {
            val track = db.trackDao().getLast()
            val points = db.trackPointDao().getByTrackId(track.id)
            return Result.success(track.toModel(points))
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }
    override suspend fun readAllTracks(): Result<List<TrackDto>> {
        try {
            //val points = db.trackPointDao().readByTrackId(id)
            val tracks = db.trackDao().getAll()
            return Result.success(tracks.map { it.toModel(listOf()) })
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }
    override suspend fun deleteTrack(id: Long): Result<Unit> {
        try {
            db.trackPointDao().deleteByTrackId(id)
            return Result.success(Unit)
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }

    override suspend fun getLastLocation(id: Long): Result<TrackPointDto> {
        try {
            db.trackPointDao().getLastByTrackId(id)?.let {
                return Result.success(it.toModel())
            } ?: run {
                return Result.failure(AppError.NotFound)
            }
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "Repo"
    }
}