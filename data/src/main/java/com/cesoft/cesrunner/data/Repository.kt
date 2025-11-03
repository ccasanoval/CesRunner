package com.cesoft.cesrunner.data

import android.content.Context
import android.location.Location
import com.cesoft.cesrunner.data.local.AppDatabase
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
import com.cesoft.cesrunner.data.location.LocationDataSource
import com.cesoft.cesrunner.data.prefs.PrefDataSource
import com.cesoft.cesrunner.domain.AppError
import com.cesoft.cesrunner.domain.ID_NULL
import com.cesoft.cesrunner.domain.entity.LocationDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackDto.Companion.VO2MAX
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.domain.repository.RepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class Repository(
    private val context: Context,
    private val locationDataSource: LocationDataSource,
    private val db: AppDatabase,
): RepositoryContract {

    /// AI AGENT
    override suspend fun filterTracks(name: String?, distance: Int?): Result<List<TrackDto>> {
        try {
            android.util.Log.e(TAG, "filterTracks------- name=$name & distance=$distance")
            val tracks: List<LocalTrackDto>? = db.trackDao().filter(name, distance)
            return if(tracks.isNullOrEmpty()) Result.failure(AppError.NotFound)
            else Result.success(tracks.map { it.toModel(listOf()) })
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }

    /// VO2MAX
    override suspend fun readVo2Max(): Double {
        var vo2max = PrefDataSource(context).readVo2Max(0.0)
        if(vo2max > VO2MAX) vo2max = 0.0
        if(vo2max == 0.0) {
            val tracks = db.trackDao().getAll()
            for(track in tracks) {
                //val timeMinutes = (track.timeEnd - track.timeIni)/60_000
                //val vo2max0 = (track.distance / timeMinutes) * 0.2 + 3.5
                val vo2max0 = track.calcVo2Max()
                if(vo2max0 > vo2max) vo2max = vo2max0
            }
            if(vo2max > 0.0) {
                PrefDataSource(context).saveVo2Max(vo2max)
            }
        }
        return vo2max
    }
    override suspend fun saveVo2Max(value: Double): Result<Unit> {
        val vo2Max = readVo2Max()
        if(value > vo2Max)
            return PrefDataSource(context).saveVo2Max(value)
        else
            return Result.failure(AppError.NotFound)
    }

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
//    override suspend fun readCurrentTrackFlow(): Result<Flow<TrackDto?>> {
//        val flow = PrefDataSource(context).readCurrentTrackingIdFlow()
//            .reduce { it?.let { readTrackFlow(it).getOrNull() } }
//        return Result.success(flow)
//    }
    override suspend fun saveCurrentTrack(id: Long): Result<Unit> {
        PrefDataSource(context).saveCurrentTrackingId(id)
        return Result.success(Unit)
    }
    override suspend fun readCurrentTrackId(): Result<Long> {
        return Result.success(PrefDataSource(context).readCurrentTrackingId(ID_NULL))
    }
    override suspend fun readCurrentTrackIdFlow(): Result<Flow<Long?>> {
        return Result.success(PrefDataSource(context).readCurrentTrackingIdFlow())
    }


    /// TRACKING SERVICE
    override fun getLastKnownLocation(): LocationDto? {
        val location = locationDataSource.getLastKnownLocation()
        return if(location == null) null else LocationDto(location.latitude, location.longitude)
    }
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
            track?.let { return Result.success(it.toModel(points)) }
                ?: run { return Result.failure(AppError.NotFound) }
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }
    override suspend fun readTrackFlow(id: Long): Result<Flow<TrackDto?>> {
        try {
            val points = db.trackPointDao().getFlowByTrackId(id)
            return Result.success(
                points.map { db.trackDao().getById(id)?.toModel(it) }
            )
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }
    override suspend fun readLastTrack(): Result<TrackDto> {
        try {
            val track = db.trackDao().getLast()
            track?.let {
                val points = db.trackPointDao().getByTrackId(track.id)
                return Result.success(track.toModel(points))
            } ?: run {
                return Result.failure(AppError.NotFound)
            }
        }
        catch(e: Throwable) {
            return Result.failure(e)
        }
    }
    override suspend fun readAllTracks(): Result<List<TrackDto>> {
        try {
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
            db.trackDao().deleteById(id)
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

    override suspend fun getNearTracks(lat: Double, lng: Double): Result<TrackDto> {
        val location = Location("point A")
        location.latitude = lat
        location.longitude = lng

        var trackId = -1L
        var distance = 1_000_000f
        val locationsX = db.trackPointDao().getAllLocations()
        for(l in locationsX) {
            val locationX = Location("point B")
            locationX.latitude = l.latitude
            locationX.longitude = l.longitude
            val d = location.distanceTo(locationX)
            if(d < distance) {
                trackId = l.idTrack
                distance = d
            }
        }
        if(trackId > -1) {
            db.trackDao().getById(trackId)?.let { track ->
                return Result.success(track.toModel(listOf()))
            }
        }
        return Result.failure(AppError.NotFound)
    }

    companion object {
        private const val TAG = "Repo"
    }
}