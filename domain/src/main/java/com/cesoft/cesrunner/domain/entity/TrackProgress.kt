package com.cesoft.cesrunner.domain.entity

/// State of the track being recorded, so each new location doesn't need to reload the whole track
data class TrackProgress(
    val trackId: Long,
    val timeIni: Long,
    val distance: Double,
    val lastPoint: TrackPointDto?,
    val pointCount: Int,
) {
    /// Returns the new progress, or null if the point is in the same place as the last one
    fun add(point: TrackPointDto): TrackProgress? {
        val last = lastPoint
        if(point.isSamePlace(last)) return null
        val delta = last?.toLocationDto()?.distanceTo(point.toLocationDto()) ?: 0.0
        return copy(
            timeIni = if(last == null) point.time else timeIni,
            distance = distance + delta,
            lastPoint = point,
            pointCount = pointCount + 1,
        )
    }

    companion object {
        fun from(track: TrackDto) = TrackProgress(
            trackId = track.id,
            timeIni = track.points.firstOrNull()?.time ?: track.timeIni,
            distance = TrackDto.calcDistance(track.points),
            lastPoint = track.points.lastOrNull(),
            pointCount = track.points.size,
        )
    }
}
