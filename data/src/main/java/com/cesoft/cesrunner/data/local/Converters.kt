package com.cesoft.cesrunner.data.local
/*
import androidx.room.TypeConverter
import java.lang.reflect.Type
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Converters {

    @TypeConverter
    fun dateToModel(time: Long): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        val instant = Instant.ofEpochMilli(time)
        val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val dateStr = formatter.format(date)
        android.util.Log.e("Converters", "dateToModel--- $dateStr ---- $time")
        return date
    }

    @TypeConverter
    fun hoursToDatabase(date: LocalDateTime): Long {
        val time = date.toInstant(ZoneOffset.UTC).toEpochMilli()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        val dateStr = formatter.format(date)
            android.util.Log.e("Converters", "hoursToDatabase--- $dateStr ---- $time")
        return time
    }

    @TypeConverter
    fun hoursToModel(data: String): Hours? {
        val gson = Gson()
        val listType: Type = object : TypeToken<Hours?>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun hoursToDatabase(hours: Hours?): String? {
        val gson = Gson()
        return gson.toJson(hours)
    }
}*/