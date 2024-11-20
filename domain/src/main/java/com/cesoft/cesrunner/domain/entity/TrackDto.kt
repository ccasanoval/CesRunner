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
    val points: List<TrackPointDto> = listOf()
) {
    val isCreated: Boolean
        get() = id != ID_NULL

    val time = timeEnd - timeIni

    fun calcDistance(location: LocationDto): Double {
//        val polyline = Polyline()
//        polyline.setPoints(track.points.map { GeoPoint(it.latitude, it.longitude) })
//        val d = polyline.distance
        val ps = points.map { LocationDto(it.latitude, it.longitude) }.toMutableList()
        location.let { ps.add(location) }
        var di = 0.0
        if(ps.isNotEmpty()) {
            var p = ps.first()
            for (i in ps) {
                di += p.distanceTo(i)
                p = i
            }
        }
        return di
    }

    companion object {
        val Empty = TrackDto(
            ID_NULL, "",
            0,0,0,0, 0,
            listOf()
        )
        fun calcDistance(points: List<TrackPointDto>): Double {
            val ps = points.map { LocationDto(it.latitude, it.longitude) }.toMutableList()
            var di = 0.0
            if(ps.isNotEmpty()) {
                var p = ps.first()
                for (i in ps) {
                    di += p.distanceTo(i)
                    p = i
                }
            }
            return di
        }
    }
}