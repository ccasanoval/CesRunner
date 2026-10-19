package com.cesoft.cesrunner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.cesoft.cesrunner.domain.entity.RunDto
import com.cesoft.cesrunner.domain.entity.TrackDto

const val TrackTableName = "track"
@Entity(tableName = TrackTableName)
data class LocalTrackDto(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val timeIni: Long,
    val timeEnd: Long,
    val distance: Int,
) {
    fun toModelDto(
        distanceToLocation: Int = -1,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ) = RunDto(
        id = id,
        name = name,
        timeIni = timeIni,
        timeEnd = timeEnd,
        distance = distance,
        distanceToLocation = distanceToLocation,
        latitude = latitude,
        longitude = longitude,
        vo2Max = calcVo2Max()
    )

    fun toModel(points: List<LocalTrackPointDto>) = TrackDto(
        id = id,
        timeIni = timeIni,
        timeEnd = timeEnd,
        name = name,
        distance = distance,
        points = points.map { it.toModel() }
    )

    fun calcVo2Max(): Double {
        return TrackDto.calcVo2Max(timeEnd, timeIni, distance)
    }

    companion object {
        fun fromModel(data: TrackDto) = LocalTrackDto(
            id = if(data.id > 0) data.id else 0,//Note that room only create new row id if it's == 0
            timeIni = data.timeIni,
            timeEnd = data.timeEnd,
            name = data.name,
            distance = data.distance,
        )
    }
}