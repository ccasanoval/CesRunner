package com.cesoft.cesrunner.domain.entity

data class TrackDto(
    val id: Long,
    val time: Long,
    val name: String,
    val distance: Long,
    val altitude: Long,
    val speed: Float,
    val points: List<TrackPointDto>
) {
    companion object {
        val Empty = TrackDto(0, 0, "", 0, 0, 0f, listOf())
    }
}