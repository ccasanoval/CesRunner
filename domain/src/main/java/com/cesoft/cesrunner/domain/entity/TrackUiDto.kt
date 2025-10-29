package com.cesoft.cesrunner.domain.entity

import com.cesoft.cesrunner.domain.ID_NULL
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class TrackUiDto(
    val id: Long = ID_NULL,
    val name: String,
    val timeIni: String,
    val timeEnd: String,
    val distance: Int,
    //
    val time: Long,
    val vo2Max: Int,
) {
    companion object {
        fun Long.toDate(showTime: Boolean = true): String? {
            if(this == 0L) return null
            val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
            val formatter = if(showTime) DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            else DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return date.format(formatter)
        }
        fun toUi(track: TrackDto) = TrackUiDto(
            id = track.id,
            name = track.name,
            timeIni = track.timeIni.toDate() ?: "?",
            timeEnd = track.timeEnd.toDate() ?: "?",
            distance = track.distance,
            time = track.timeEnd - track.timeIni,
            vo2Max = track.calcVo2Max().toInt()
        )
    }
}