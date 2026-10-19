package com.cesoft.cesrunner.data

import android.location.Location
import androidx.room.withTransaction
import com.cesoft.cesrunner.data.groq.Groq
import com.cesoft.cesrunner.data.local.AppDatabase
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
import com.cesoft.cesrunner.data.location.LocationDataSource
import com.cesoft.cesrunner.data.prefs.PrefDataSource
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.ID_NULL
import com.cesoft.cesrunner.domain.entity.AIAgentRes
import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.entity.LocationDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackDto.Companion.VO2MAX
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Repository(
    private val prefs: PrefDataSource,
    private val locationDataSource: LocationDataSource,
    private val db: AppDatabase,
    private val groq: Groq
): RepositoryContract {

    /// AI AGENT : GROQ
    override suspend fun askGroq(prompt: String): Result<AIAgentRes> {
        return groq.chat(prompt)
    }

    /// AI AGENT : KOOG
    override suspend fun filterTracks(name: String?, distance: Int?): Result<List<TrackDto>> =
        runCatching {
            val tracks = db.trackDao().filter(null, name, distance)
            if(tracks.isEmpty()) throw AppError.NotFound
            tracks.map { it.toModel(db.trackPointDao().getByTrackId(it.id)) }
        }

    override suspend fun getNearTracks(
        lat: Double, lng: Double,
        distanceMax: Int
    ): Result<List<TrackDto>> = runCatching {
        val location = LocationDto(lat, lng)
        val tracksId = linkedSetOf<Long>()
        for(point in db.trackPointDao().getAllLocations()) {
            if(point.idTrack in tracksId) continue
            if(location.distanceTo(LocationDto(point.latitude, point.longitude)) < distanceMax) {
                tracksId.add(point.idTrack)
            }
        }
        tracksId.mapNotNull { db.trackDao().getById(it)?.toModel(listOf()) }
    }

    /// VO2MAX
    override suspend fun readVo2Max(): Double {
        var vo2max = prefs.readVo2Max(0.0)
        if(vo2max > VO2MAX) vo2max = 0.0
        if(vo2max == 0.0) {
            vo2max = db.trackDao().getAll().maxOfOrNull { it.calcVo2Max() } ?: 0.0
            if(vo2max > 0.0) {
                prefs.saveVo2Max(vo2max)
            }
        }
        return vo2max
    }
    /// Only stores the value if it's a new record
    override suspend fun saveVo2Max(value: Double): Result<Unit> {
        return if(value > readVo2Max()) prefs.saveVo2Max(value)
        else Result.success(Unit)
    }

    /// PREFS
    override suspend fun readSettings(): Result<SettingsDto> = prefs.readSettings()
    override suspend fun saveSettings(data: SettingsDto): Result<Unit> = prefs.saveSettings(data)
    override suspend fun readApiKeys(): Result<ApiKeysDto> = prefs.readApiKeys()
    override suspend fun saveApiKeys(data: ApiKeysDto): Result<Unit> = prefs.saveApiKeys(data)
    override suspend fun readCurrentTrack(): Result<TrackDto> {
        val id = prefs.readCurrentTrackingId(ID_NULL)
        return if(id > ID_NULL) readTrack(id) else Result.failure(AppError.NotFound)
    }
    override suspend fun saveCurrentTrack(id: Long): Result<Unit> {
        prefs.saveCurrentTrackingId(id)
        return Result.success(Unit)
    }
    override suspend fun readCurrentTrackId(): Result<Long> {
        return Result.success(prefs.readCurrentTrackingId(ID_NULL))
    }
    override suspend fun readCurrentTrackIdFlow(): Result<Flow<Long?>> {
        return Result.success(prefs.readCurrentTrackingIdFlow())
    }

    /// LOCATION
    override fun getLastKnownLocation(): LocationDto? =
        locationDataSource.getLastKnownLocation()?.let { LocationDto(it.latitude, it.longitude) }
    override fun requestLocationUpdates(minInterval: Long, minDistance: Float): Flow<TrackPointDto> =
        locationDataSource.requestLocationUpdates(minInterval, minDistance).map { it.toTrackPointDto() }

    /// TRACKING DATABASE
    override suspend fun createTrack(data: TrackDto): Result<Long> = runCatching {
        val id: Long = db.trackDao().create(LocalTrackDto.fromModel(data))
        if(id > 0) id else throw AppError.DataBaseError(IllegalStateException("Track not created"))
    }
    override suspend fun updateTrack(data: TrackDto): Result<Unit> = runCatching {
        db.trackDao().update(LocalTrackDto.fromModel(data))
    }
    override suspend fun addTrackPoint(
        id: Long,
        point: TrackPointDto,
        distance: Int,
        timeIni: Long
    ): Result<Unit> = runCatching {
        db.withTransaction {
            db.trackPointDao().add(LocalTrackPointDto.fromModel(id, point))
            db.trackDao().updateProgress(id, distance, timeIni, point.time)
        }
    }
    override suspend fun readTrack(id: Long): Result<TrackDto> = runCatching {
        val track = db.trackDao().getById(id) ?: throw AppError.NotFound
        track.toModel(db.trackPointDao().getByTrackId(id))
    }
    override suspend fun readTrackFlow(id: Long): Result<Flow<TrackDto?>> = runCatching {
        db.trackPointDao().getFlowByTrackId(id)
            .map { points -> db.trackDao().getById(id)?.toModel(points) }
    }
    override suspend fun readLastTrack(): Result<TrackDto> = runCatching {
        val track = db.trackDao().getLast() ?: throw AppError.NotFound
        track.toModel(db.trackPointDao().getByTrackId(track.id))
    }
    override suspend fun readAllTracks(): Result<List<TrackDto>> = runCatching {
        db.trackDao().getAll().map { it.toModel(listOf()) }
    }
    override suspend fun deleteTrack(id: Long): Result<Unit> = runCatching {
        db.withTransaction {
            db.trackPointDao().deleteByTrackId(id)
            db.trackDao().deleteById(id)
        }
    }

    companion object {
        internal fun Location.toTrackPointDto() = TrackPointDto(
            latitude = latitude,
            longitude = longitude,
            time = time,
            accuracy = accuracy,
            provider = provider ?: "?",
            altitude = altitude,
            bearing = bearing,
            speed = speed
        )
    }
}
