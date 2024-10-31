package com.cesoft.cesrunner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cesoft.cesrunner.domain.entity.TrackPointDto

const val TrackPointTableName = "track_point"
@Entity(tableName = TrackPointTableName)
data class LocalTrackPointDto(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val idTrack: Long,
    val latitude: Double,
    val longitude: Double,
    val time: Long,
    val accuracy: Float,
    val provider: String,
    val altitude: Double,
    val bearing: Float,
    val speed: Float,
) {
    fun toModel() = TrackPointDto(
        id = id,
        latitude = latitude,
        longitude = longitude,
        time = time,
        accuracy = accuracy,
        provider = provider,
        altitude = altitude,
        bearing = bearing,
        speed = speed,
    )
    companion object {
        fun fromModel(idTrack: Long, data: TrackPointDto) = LocalTrackPointDto(
            id = data.id,
            idTrack = idTrack,
            latitude = data.latitude,
            longitude = data.longitude,
            time = data.time,
            accuracy = data.accuracy,
            provider = data.provider,
            altitude = data.altitude,
            bearing = data.bearing,
            speed = data.speed,
        )
    }
}