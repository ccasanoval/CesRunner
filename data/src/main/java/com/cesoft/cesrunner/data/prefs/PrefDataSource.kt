package com.cesoft.cesrunner.data.prefs

import android.content.Context
import com.cesoft.cesrunner.domain.entity.ApiKeysDto
import com.cesoft.cesrunner.domain.entity.SettingsDto
import kotlinx.coroutines.flow.Flow

class PrefDataSource(
    private val context: Context
) {
    //----------------------------------------------------------------------------------------------
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

    //----------------------------------------------------------------------------------------------
    // Stored in the app private DataStore: not readable by other apps and not shipped in the APK
    suspend fun readApiKeys(): Result<ApiKeysDto> = Result.success(
        ApiKeysDto(
            groq = context.readString(PREFS_KEY_GROQ),
            gemini = context.readString(PREFS_KEY_GEMINI),
            openAi = context.readString(PREFS_KEY_OPENAI),
            openRouter = context.readString(PREFS_KEY_OPENROUTER),
            deepSeek = context.readString(PREFS_KEY_DEEPSEEK),
        )
    )
    suspend fun saveApiKeys(data: ApiKeysDto): Result<Unit> {
        context.writeString(PREFS_KEY_GROQ, data.groq.trim())
        context.writeString(PREFS_KEY_GEMINI, data.gemini.trim())
        context.writeString(PREFS_KEY_OPENAI, data.openAi.trim())
        context.writeString(PREFS_KEY_OPENROUTER, data.openRouter.trim())
        context.writeString(PREFS_KEY_DEEPSEEK, data.deepSeek.trim())
        return Result.success(Unit)
    }

    //----------------------------------------------------------------------------------------------
    suspend fun readVo2Max(default: Double): Double =
        context.readDouble(PREFS_VO2MAX, default)
    suspend fun saveVo2Max(value: Double): Result<Unit> {
        context.writeDouble(PREFS_VO2MAX, value)
        return Result.success(Unit)
    }

    //----------------------------------------------------------------------------------------------
    suspend fun readCurrentTrackingId(default: Long): Long {
        return context.readLong(PREFS_CURRENT_TRACK, default)
    }
    fun readCurrentTrackingIdFlow(): Flow<Long?> {
        return context.readLongFlow(PREFS_CURRENT_TRACK)
    }
    suspend fun saveCurrentTrackingId(id: Long) {
        return context.writeLong(PREFS_CURRENT_TRACK, id)
    }

    //----------------------------------------------------------------------------------------------
    // Constants
    companion object {
        private const val PREFS_SETTINGS_PERIOD = "PREFS_SETTINGS_PERIOD"
        private const val PREFS_SETTINGS_DISTANCE = "PREFS_SETTINGS_DISTANCE"
        private const val PREFS_SETTINGS_VOICE = "PREFS_SETTINGS_VOICE"

        private const val PREFS_KEY_GROQ = "PREFS_KEY_GROQ"
        private const val PREFS_KEY_GEMINI = "PREFS_KEY_GEMINI"
        private const val PREFS_KEY_OPENAI = "PREFS_KEY_OPENAI"
        private const val PREFS_KEY_OPENROUTER = "PREFS_KEY_OPENROUTER"
        private const val PREFS_KEY_DEEPSEEK = "PREFS_KEY_DEEPSEEK"

        private const val PREFS_CURRENT_TRACK = "PREFS_CURRENT_TRACK"

        private const val PREFS_VO2MAX = "PREFS_VO2MAX"
    }
}
