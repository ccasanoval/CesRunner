package com.cesoft.cesrunner.data.local

import com.cesoft.cesrunner.data.local.entity.TrackingDto

class LocalDataSource(
    private val database: AppDatabase,
) {
    suspend fun addTracking(tracking: TrackingDto) = database.trackingDao().add(tracking)
    suspend fun clearTracking() = database.trackingDao().deleteAll()
    suspend fun getTracking() = database.trackingDao().getAll()
}