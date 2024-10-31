package com.cesoft.cesrunner.domain.repository

import android.location.Location
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import kotlinx.coroutines.flow.MutableStateFlow

interface RepositoryContract {
    // PREFS
    suspend fun readSettings(): Result<SettingsDto>
    suspend fun saveSettings(data: SettingsDto): Result<Unit>
    suspend fun readCurrentTracking(): Result<CurrentTrackingDto>
    suspend fun saveCurrentTracking(data: CurrentTrackingDto): Result<Unit>

    // TRACKING SERVICE
    fun requestLocationUpdates(): Result<MutableStateFlow<Location?>>
    fun stopLocationUpdates(): Result<Unit>

    // TRACKING DATABASE
    suspend fun createTrack(data: TrackDto): Result<Unit>
    suspend fun addTrackPoint(id: Long, data: TrackPointDto): Result<Unit>
    suspend fun readTrack(id: Long): Result<TrackDto>
    suspend fun readAllTrack(): Result<List<TrackDto>>
    suspend fun deleteTrack(id: Long): Result<Unit>

}