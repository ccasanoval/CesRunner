package com.cesoft.cesrunner.ui.aiagent.ai

import ai.koog.agents.core.tools.annotations.LLMDescription
import com.cesoft.cesrunner.domain.entity.TrackDto
//import com.cesoft.cesrunner.domain.entity.TrackUiDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    val vo2Max: Int,
//    @property:LLMDescription("Coordinates of the location")
//    val latLon: LatLon
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
            val h = toString()
            val m = ((this - toInt())*60).toString().padStart(2, '0')
            return "${h}h ${m}m"
        }
        fun toUi(track: TrackDto) = RunEntity(
            id = track.id,
            name = track.name,
            timeIni = track.timeIni.toDate() ?: "?",
            timeEnd = track.timeEnd.toDate() ?: "?",
            distance = track.distance,
            time = (track.timeEnd - track.timeIni).toHoursMinutes(),
            vo2Max = track.calcVo2Max().toInt()
        )
    }
//    @Serializable
//    @SerialName("LatLon")
//    data class LatLon(
//        @property:LLMDescription("Latitude of the run")
//        val lat: Double,
//        @property:LLMDescription("Longitude of the run")
//        val lon: Double
//    )
}