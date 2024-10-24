package com.cesoft.cesrunner.data.prefs

import android.content.Context
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

    suspend fun saveSettings(settings: SettingsDto): Result<Unit> {
        context.writeInt(PREFS_SETTINGS_PERIOD, settings.period)
        return Result.success(Unit)
    }

    suspend fun readSettings(): Result<SettingsDto> {
        val period = context.readInt(PREFS_SETTINGS_PERIOD) ?: PREFS_SETTINGS_PERIOD_DEF
        return Result.success(
            SettingsDto(
                period = period
            )
        )
    }

    //----------------------------------------------------------------------------------------------
    // Constants
    companion object {
        private const val PREFS_SETTINGS_PERIOD = "PREFS_SETTINGS_PERIOD"
        private const val PREFS_SETTINGS_PERIOD_DEF = 5
//        suspend fun setNavigateFrom(context: Context, fromId: Int) {
//            context.writeInt(PREFS_NAVIGATE_FROM, fromId)
//        }
//        suspend fun getNavigateFrom(context: Context): Int? {
//            return context.readInt(PREFS_NAVIGATE_FROM)
//        }
    }
}
