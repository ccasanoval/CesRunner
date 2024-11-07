package com.cesoft.cesrunner

import android.content.Context
import android.location.Location
import com.cesoft.cesrunner.domain.entity.TrackPointDto
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toDateStr(): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    val instant = Instant.ofEpochMilli(this)
    val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val dateStr = formatter.format(date)
    //android.util.Log.e("Converters", "dateToModel--- $dateStr ---- $this")
    return dateStr
}

fun Long.toTimeStr(): String {
    var str = ""
    val m = 60
    val h = 60*m
    val d = 24*h
    val time = this / 1000  // milliseconds to seconds
    val days = time / d
    val hours = (time - days*d) / h
    val minutes = (time - days*d - hours*h) / m
    val seconds = (time - days*d - hours*h - minutes*m)
    if(days > 0) str = "${days}d "
    str += "${hours}h ${minutes}m ${seconds}s"
    //if(hours > 0) str += "${hours}h "
    //if(minutes > 0) str += "${minutes}m "
    //if(seconds > 0) str += "${seconds}s "
    return str
}

fun Long.toTimeSpeech(context: Context): String {
    val dStr = context.getString(R.string.days)
    val hStr = context.getString(R.string.hours)
    val mStr = context.getString(R.string.minutes)
    val sStr = context.getString(R.string.seconds)
    var str = ""
    val m = 60
    val h = 60*m
    val d = 24*h
    val time = this / 1000  // milliseconds to seconds
    val days = time / d
    val hours = (time - days*d) / h
    val minutes = (time - days*d - hours*h) / m
    val seconds = (time - days*d - hours*h - minutes*m)
    if(days > 0) str = "$days $dStr "
    if(hours > 0) str += ", $hours $hStr "
    str += ", $minutes $mStr, $seconds $sStr"
    return str
}

fun Location.toTrackPointDto() = TrackPointDto(
    latitude = latitude,
    longitude = longitude,
    time = time,
    accuracy = accuracy,
    provider = provider ?: "?",
    altitude = altitude,
    bearing = bearing,
    speed = speed
)