package com.cesoft.cesrunner.domain.entity

import com.cesoft.cesrunner.domain.Common.ID_NULL

data class TrackDto(
    val id: Long = ID_NULL,
    val time: Long = 0,
    val name: String = "",
    val distance: Long = 0,
    val altitude: Long = 0,
    val speed: Float = 0f,
    val points: List<TrackPointDto> = listOf()
) {
    val isCreated: Boolean
        get() = id != ID_NULL
    companion object {
        val Empty = TrackDto(ID_NULL, 0, "", 0, 0, 0f, listOf())
    }
}