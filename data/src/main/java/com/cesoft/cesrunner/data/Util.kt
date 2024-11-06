package com.cesoft.cesrunner.data

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
    str += "$hours:$minutes:$seconds"
    //if(hours > 0) str += "${hours}h "
    //if(minutes > 0) str += "${minutes}m "
    //if(seconds > 0) str += "${seconds}s "
    return str
}