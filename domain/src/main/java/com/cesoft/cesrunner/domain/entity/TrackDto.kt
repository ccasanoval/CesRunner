package com.cesoft.cesrunner.domain.entity

import com.cesoft.cesrunner.domain.ID_NULL

data class TrackDto(
    val id: Long = ID_NULL,
    val name: String = "",
    val timeIni: Long = 0,
    val timeEnd: Long = 0,
    val distance: Int = 0,
    val points: List<TrackPointDto> = listOf()
) {
    val isCreated: Boolean
        get() = id != ID_NULL

    val time = timeEnd - timeIni

    fun calcVo2Max(): Double {
        return calcVo2Max(timeEnd, timeIni, distance)
    }

    companion object {
        val Empty = TrackDto()
        const val VO2MAX = 150.0
        fun calcVo2Max(timeEnd: Long, timeIni: Long, distance: Int): Double {
            val time = (timeEnd - timeIni) / 60_000.0
            if(time <= 0.0 || distance <= 0) return 0.0 // Avoid NaN / Infinity / negative values
            val vo2Max = (distance / time)*0.2 + 3.5
            // Current vo2max record is bellow 100
            if(vo2Max > VO2MAX) return 0.0 //If you get a car or a flight, you are not fooling anyone
            return vo2Max
        }
        fun calcDistance(points: List<TrackPointDto>): Double =
            points.zipWithNext { a, b -> a.toLocationDto().distanceTo(b.toLocationDto()) }.sum()
    }
}