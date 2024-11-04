package com.cesoft.cesrunner.domain.entity

import com.cesoft.cesrunner.domain.Common.ID_NULL

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
)