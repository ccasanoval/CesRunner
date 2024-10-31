package com.cesoft.cesrunner.domain.entity

data class TrackPointDto(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val time: Long,
    val accuracy: Float,
    val provider: String,
    val altitude: Double,
    val bearing: Float,
    val speed: Float,
)