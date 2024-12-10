package com.cesoft.cesrunner.domain.entity

import com.cesoft.cesrunner.domain.ID_NULL

data class TrackPointDto(
    val id: Long = ID_NULL,
    val latitude: Double,
    val longitude: Double,
    val time: Long,
    val accuracy: Float,
    val provider: String,
    val altitude: Double,
    val bearing: Float,
    val speed: Float,
) {
    //fun toLocationDto() = LocationDto(latitude, longitude)
    companion object {
        val Empty = TrackPointDto(ID_NULL, 0.0, 0.0,
            0, 0f,"",0.0,0f,0f)
    }
}