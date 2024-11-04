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