package com.cesoft.cesrunner.domain.repository

import com.cesoft.cesrunner.domain.entity.AIAgentRes
import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.entity.LocationDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import kotlinx.coroutines.flow.Flow

interface RepositoryContract {
    // AI AGENT
    suspend fun filterTracks(name: String?, distance: Int?): Result<List<TrackDto>>

    // VO2MAX
    suspend fun readVo2Max(): Double
    suspend fun saveVo2Max(value: Double): Result<Unit>

    // PREFS
    suspend fun readSettings(): Result<SettingsDto>
    suspend fun saveSettings(data: SettingsDto): Result<Unit>
    suspend fun readApiKeys(): Result<ApiKeysDto>
    suspend fun saveApiKeys(data: ApiKeysDto): Result<Unit>
    suspend fun saveCurrentTrack(id: Long): Result<Unit>
    suspend fun readCurrentTrackId(): Result<Long>
    suspend fun readCurrentTrackIdFlow(): Result<Flow<Long?>>
    suspend fun readCurrentTrack(): Result<TrackDto>

    // LOCATION
    fun getLastKnownLocation(): LocationDto?
    /// Cold flow: location updates start when collected and stop when the collector is cancelled
    fun requestLocationUpdates(minInterval: Long, minDistance: Float): Flow<TrackPointDto>

    // TRACKING DATABASE
    suspend fun createTrack(data: TrackDto): Result<Long>
    suspend fun updateTrack(data: TrackDto): Result<Unit>
    /// Stores the point and updates the track distance and times in a single transaction
    suspend fun addTrackPoint(id: Long, point: TrackPointDto, distance: Int, timeIni: Long): Result<Unit>
    suspend fun readTrack(id: Long): Result<TrackDto>
    suspend fun readTrackFlow(id: Long): Result<Flow<TrackDto?>>
    suspend fun readLastTrack(): Result<TrackDto>
    suspend fun readAllTracks(): Result<List<TrackDto>>
    suspend fun deleteTrack(id: Long): Result<Unit>
    suspend fun getNearTracks(lat: Double, lng: Double, distanceMax: Int): Result<List<TrackDto>>

    // GROQ
    suspend fun askGroq(prompt: String): Result<AIAgentRes>
}
