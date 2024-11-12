package com.cesoft.cesrunner

import android.content.Context
import android.location.Location
import androidx.compose.ui.text.intl.Locale
import com.cesoft.cesrunner.domain.AppError
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

fun Int.toDistanceStr(): String {
    return if(this < 1000) "$this m"
    else {
        val a = (this/100)/10f
        val b = this / 1000
        if(a == b.toFloat()) String.format(Locale.current.platformLocale,"%d Km", b)
        else String.format(Locale.current.platformLocale,"%.1f Km", a)
    }
}

enum class MessageType { Saved }
fun MessageType.toStr(context: Context) = when(this) {
    MessageType.Saved -> context.getString(R.string.saved)
}

fun AppError.toStr(context: Context) = when(this) {
    AppError.NotFound -> context.getString(R.string.error_not_found)
    is AppError.DataBaseError -> context.getString(R.string.error_db) + ": "+this.message
    else -> this.message
}