package com.cesoft.cesrunner.data.prefs

import android.content.Context
import com.cesoft.cesrunner.domain.entity.SettingsDto
import com.cesoft.cesrunner.domain.entity.TrackDto


class PrefDataSource(
    private val context: Context
) {

//    private fun zoningToModel(data: String): List<Long>? {
//        val gson = Gson()
//        val listType: Type = object : TypeToken<List<Long>>() {}.type
//        return gson.fromJson(data, listType)
//    }
//    private fun zoningToDto(zoning: List<Long>): String {
//        val gson = Gson()
//        return gson.toJson(zoning)
//    }

    suspend fun readSettings(): Result<SettingsDto> {
        val period = context.readInt(PREFS_SETTINGS_PERIOD, SettingsDto.DEFAULT_PERIOD)
        val distance = context.readInt(PREFS_SETTINGS_DISTANCE, SettingsDto.DEFAULT_DISTANCE)
        val voice = context.readBool(PREFS_SETTINGS_VOICE, SettingsDto.DEFAULT_VOICE)
        return Result.success(
            SettingsDto(
                minInterval = period,
                minDistance = distance,
                voice = voice
            )
        )
    }
    suspend fun saveSettings(data: SettingsDto): Result<Unit> {
        context.writeInt(PREFS_SETTINGS_PERIOD, data.minInterval)
        context.writeInt(PREFS_SETTINGS_DISTANCE, data.minDistance)
        context.writeBool(PREFS_SETTINGS_VOICE, data.voice)
        return Result.success(Unit)
    }

    suspend fun readCurrentTrackingId(default: Long): Long {
        return context.readLong(PREFS_CURRENT_TRACK, default)
    }
    suspend fun saveCurrentTrackingId(id: Long) {
        return context.writeLong(PREFS_CURRENT_TRACK, id)
    }

//    suspend fun readCurrentTracking(): Result<CurrentTrackingDto> {
//        val isTracking = context.readBool(PREFS_CURRENT_IS_TRACKING, false)
//        android.util.Log.e(TAG, "readCurrentTracking------- isTracking=$isTracking")
//        return Result.success(
//            CurrentTrackingDto(
//                isTracking = isTracking
//            )
//        )
//    }
//    suspend fun saveCurrentTracking(data: CurrentTrackingDto): Result<Unit> {
//        context.writeBool(PREFS_CURRENT_IS_TRACKING, data.isTracking)
//        return Result.success(Unit)
//    }

    //----------------------------------------------------------------------------------------------
    // Constants
    companion object {
        private const val TAG = "PrefDS"

        private const val PREFS_SETTINGS_PERIOD = "PREFS_SETTINGS_PERIOD"
        private const val PREFS_SETTINGS_DISTANCE = "PREFS_SETTINGS_DISTANCE"
        private const val PREFS_SETTINGS_VOICE = "PREFS_SETTINGS_VOICE"

        private const val PREFS_CURRENT_TRACK = "PREFS_CURRENT_TRACK"

//        suspend fun setNavigateFrom(context: Context, fromId: Int) {
//            context.writeInt(PREFS_NAVIGATE_FROM, fromId)
//        }
//        suspend fun getNavigateFrom(context: Context): Int? {
//            return context.readInt(PREFS_NAVIGATE_FROM)
//        }
    }
}
