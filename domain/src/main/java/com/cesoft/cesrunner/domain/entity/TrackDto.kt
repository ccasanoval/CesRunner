package com.cesoft.cesrunner.domain.entity

import com.cesoft.cesrunner.domain.Common.ID_NULL

data class TrackDto(
    val id: Long = ID_NULL,
    val name: String = "",
    val minInterval: Int = 0,
    val minDistance: Int = 0,
    val timeIni: Long = 0,
    val timeEnd: Long = 0,
    val distance: Int = 0,
    /*val altitudeMin: Int = 0,
    val altitudeMax: Int = 0,
    val speedMin: Int = 0,
    val speedMax: Int = 0,*/
    val points: List<TrackPointDto> = listOf()
) {
    val isCreated: Boolean
        get() = id != ID_NULL
    val time = timeEnd - timeIni
    companion object {
        val Empty = TrackDto(
            ID_NULL, "",
            0,0,0,0, 0,
            listOf()
        )
    }
}