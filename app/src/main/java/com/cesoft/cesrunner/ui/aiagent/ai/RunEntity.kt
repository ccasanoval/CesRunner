package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.tools.annotations.LLMDescription
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import com.cesoft.cesrunner.ui.aiagent.ai.RunEntity.Location.Companion.toLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.DurationUnit
import kotlin.time.toDuration

//https://docs.koog.ai/structured-output/
@Serializable
@SerialName("RunEntity")
@LLMDescription("Data that describes a run")
data class RunEntity(
    @property:LLMDescription("Unique identifier or ID of the run")
    val id: Long,
    @property:LLMDescription("Name of the run")
    val name: String,
    @property:LLMDescription("Start time of the run, time when the run initiated")
    val timeIni: String,
    @property:LLMDescription("End time of the run, time when the run stoped")
    val timeEnd: String,
    @property:LLMDescription("Distance of the run in meters")
    val distance: Int,
    @property:LLMDescription("Duration of the run, equal to timeEnd minus timeIni")
    val time: String,
    @property:LLMDescription("Vo2Max of the run, or the maximum rate of oxygen consumption attainable during physical exertion during the run, an indicator of cardiovascular fitness")
    val vo2Max: Double,
    @property:LLMDescription("Coordinates of the location")
    val location: Location
) {
    companion object {
        fun Long.toDate(showTime: Boolean = true): String? {
            if(this == 0L) return null
            val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
            val formatter = if(showTime) DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            else DateTimeFormatter.ofPattern("dd/MM/yyyy")
            return date.format(formatter)
        }
        fun Long.toHoursMinutes(): String {
            val duration = toDuration(DurationUnit.MILLISECONDS)
            val h = duration.inWholeHours
            val m = duration.inWholeMinutes
            if(h > 0) return "${h}h ${m}m"
            else return "${m}m"
        }
        fun TrackDto.toRunEntity() = RunEntity(
            id = id,
            name = name,
            timeIni = timeIni.toDate() ?: "?",
            timeEnd = timeEnd.toDate() ?: "?",
            distance = distance,
            time = (timeEnd - timeIni).toHoursMinutes(),
            vo2Max = calcVo2Max(),
            location = points.firstOrNull()?.toLocation() ?: Location.Empty
        )
        fun toUi(track: TrackDto) = RunEntity(
            id = track.id,
            name = track.name,
            timeIni = track.timeIni.toDate() ?: "?",
            timeEnd = track.timeEnd.toDate() ?: "?",
            distance = track.distance,
            time = (track.timeEnd - track.timeIni).toHoursMinutes(),
            vo2Max = track.calcVo2Max(),
            location = track.points.firstOrNull()?.toLocation() ?: Location.Empty
        )
    }
    @Serializable
    @SerialName("Location")
    data class Location(
        @property:LLMDescription("Latitude of the run")
        val latitude: Double,
        @property:LLMDescription("Longitude of the run")
        val longitude: Double
    ) {
        companion object {
            fun TrackPointDto.toLocation() = Location(latitude, longitude)
            val Empty = Location(0.0, 0.0)
        }
    }
}