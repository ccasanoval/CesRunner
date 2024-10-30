package com.cesoft.cesrunner.data.prefs

import android.content.Context
import com.cesoft.cesrunner.domain.entity.CurrentTrackingDto
import com.cesoft.cesrunner.domain.entity.SettingsDto


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
        val period = context.readInt(PREFS_SETTINGS_PERIOD) ?: PREFS_SETTINGS_PERIOD_DEF
        return Result.success(
            SettingsDto(
                period = period
            )
        )
    }
    suspend fun saveSettings(data: SettingsDto): Result<Unit> {
        context.writeInt(PREFS_SETTINGS_PERIOD, data.period)
        return Result.success(Unit)
    }

    suspend fun readCurrentTracking(): Result<CurrentTrackingDto> {
        val isTracking = context.readBool(PREFS_CURRENT_IS_TRACKING, false)
        android.util.Log.e(TAG, "readCurrentTracking------- isTracking=$isTracking")
        return Result.success(
            CurrentTrackingDto(
                isTracking = isTracking
            )
        )
    }
    suspend fun saveCurrentTracking(data: CurrentTrackingDto): Result<Unit> {
        context.writeBool(PREFS_CURRENT_IS_TRACKING, data.isTracking)
        return Result.success(Unit)
    }


    //----------------------------------------------------------------------------------------------
    // Constants
    companion object {
        private const val TAG = "PrefDS"

        private const val PREFS_SETTINGS_PERIOD = "PREFS_SETTINGS_PERIOD"
        private const val PREFS_SETTINGS_PERIOD_DEF = 5

        private const val PREFS_CURRENT_IS_TRACKING = "PREFS_CURRENT_IS_TRACKING"

//        suspend fun setNavigateFrom(context: Context, fromId: Int) {
//            context.writeInt(PREFS_NAVIGATE_FROM, fromId)
//        }
//        suspend fun getNavigateFrom(context: Context): Int? {
//            return context.readInt(PREFS_NAVIGATE_FROM)
//        }
    }
}
