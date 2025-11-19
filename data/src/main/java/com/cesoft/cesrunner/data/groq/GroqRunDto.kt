package com.cesoft.cesrunner.data.groq

import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
import com.cesoft.cesrunner.domain.ID_NULL
import com.cesoft.cesrunner.domain.entity.TrackDto
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/*data class GroqRunPointDto(
    val id: Long = ID_NULL,
    val latitude: Double,
    val longitude: Double,
    val time: Long,
    val accuracy: Float,
    val provider: String,
    val altitude: Double,
    val bearing: Float,
    val speed: Float,
)*/

//TODO: To domain, where it belongs as a general class: data class Runs() that combine track and trackPoints to be consumed as it is the real business object
data class GroqRunDto(
    val id: Long,
    val name: String,
    val timeIni: String,//TODO: Better millis since 1970
    val timeEnd: String,//TODO: Better millis since 1970
    val distance: Int,
    val distanceToLocation: Int,
    val time: String,//TODO: Remove....
    val timeMillis: Long,
    val vo2Max: Double,
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        val EMPTY = GroqRunDto(-1, "", "", "", 0, 0, "", 0, 0.0, 0.0, 0.0)
        fun Long.toDate(showTime: Boolean = true): String? {
            if (this == 0L) return null
            val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
            val formatter = if (showTime) DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            else DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return date.format(formatter)
        }
        fun Long.toHoursMinutes(): String {
            val duration = toDuration(DurationUnit.MILLISECONDS)
            val h = duration.inWholeHours
            val m = duration.inWholeMinutes
            return if (h > 0) "${h}h ${m}m"
            else "${m}m"
        }
        fun LocalTrackDto.toRun(
            points: List<LocalTrackPointDto> = listOf(),
            distanceToLocation: Int = 0
        ) = GroqRunDto(
            id = id,
            name = name,
            timeIni = timeIni.toDate() ?: "?",
            timeEnd = timeEnd.toDate() ?: "?",
            distance = distance,
            distanceToLocation = distanceToLocation,
            time = (timeEnd - timeIni).toHoursMinutes(),
            timeMillis = timeEnd - timeIni,
            vo2Max = calcVo2Max(),
            latitude = points.firstOrNull()?.latitude ?: 0.0,
            longitude = points.firstOrNull()?.longitude ?: 0.0,
        )
    }
}