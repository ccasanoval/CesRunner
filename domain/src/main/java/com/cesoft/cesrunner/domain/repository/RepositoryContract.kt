package com.cesoft.cesrunner.domain.repository

import android.location.Location
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import kotlinx.coroutines.flow.MutableStateFlow

interface RepositoryContract {
    // PREFS
    suspend fun readSettings(): Result<SettingsDto>
    suspend fun saveSettings(data: SettingsDto): Result<Unit>
    suspend fun readCurrentTrack(): Result<TrackDto>
    suspend fun saveCurrentTrack(id: Long): Result<Unit>

    // TRACKING SERVICE
    fun requestLocationUpdates(): Result<MutableStateFlow<Location?>>
    fun stopLocationUpdates(): Result<Unit>

    // TRACKING DATABASE
    suspend fun createTrack(data: TrackDto): Result<Long>
    suspend fun updateTrack(data: TrackDto): Result<Unit>
    suspend fun addTrackPoint(id: Long, data: TrackPointDto): Result<Unit>
    suspend fun readTrack(id: Long): Result<TrackDto>
    suspend fun readAllTracks(): Result<List<TrackDto>>
    suspend fun deleteTrack(id: Long): Result<Unit>
    suspend fun getLastLocation(id: Long): Result<TrackPointDto>

}