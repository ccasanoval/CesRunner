package com.cesoft.cesrunner

import android.content.Context
import com.cesoft.cesrunner.domain.AppError
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Long.toDateStr(): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    val instant = Instant.ofEpochMilli(this)
    val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    return formatter.format(date)
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
    if(minutes > 0) str += ", $minutes $mStr"
    str += ", $seconds $sStr"
    return str
}

fun Int.toDistanceStr(): String {
    return if(this < 1000) "$this m"
    else {
        val a = (this/100)/10f
        val b = this / 1000
        if(a == b.toFloat()) String.format(Locale.getDefault(),"%d Km", b)
        else String.format(Locale.getDefault(),"%.1f Km", a)
    }
}

fun Int.toDistanceSpeech(context: Context): String {
    val m = context.getString(R.string.meters)
    val km = context.getString(R.string.kilometers)
    return if(this < 1000) "$this $m"
    else {
        val a = (this/100)/10f
        val b = this / 1000
        if(a == b.toFloat()) String.format(Locale.getDefault(),"%d $km", b)
        else String.format(Locale.getDefault(),"%.1f $km", a)
    }
}

enum class MessageType { Saved, Exported, Deleted }
fun MessageType.toStr(context: Context) = when(this) {
    MessageType.Saved -> context.getString(R.string.saved_ok)
    MessageType.Exported -> context.getString(R.string.exported_ok)
    MessageType.Deleted -> context.getString(R.string.deleted_ok)
}

fun AppError.toStr(context: Context): String = when(this) {
    AppError.NotFound -> context.getString(R.string.error_not_found)
    AppError.MissingApiKey -> context.getString(R.string.error_missing_api_key)
    is AppError.DataBaseError -> context.getString(R.string.error_db) + (e.message?.let { ": $it" } ?: "")
    else -> message ?: toString()
}

fun Throwable.toUserMessage(context: Context): String =
    if(this is AppError) toStr(context) else localizedMessage ?: message ?: toString()
