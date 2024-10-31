package com.cesoft.cesrunner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto

const val TrackTableName = "track"
@Entity(tableName = TrackTableName)
data class LocalTrackDto(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val time: Long,
    val name: String,
    val distance: Long,
    val altitude: Long,
    val speed: Float,
) {
    fun toModel(points: List<LocalTrackPointDto>) = TrackDto(
        id = id,
        time = time,
        name = name,
        distance = distance,
        altitude = altitude,
        speed = speed,
        points = points.map { it.toModel() }
    )

    companion object {
        fun fromModel(data: TrackDto) = LocalTrackDto(
            id = data.id,
            time = data.time,
            name = data.name,
            distance = data.distance,
            altitude = data.altitude,
            speed = data.speed,
        )
    }
}