package com.cesoft.cesrunner.domain.entity

data class SettingsDto(
    val minInterval: Int,
    val minDistance: Int,
    val voice: Boolean,
) {
    companion object {
        const val DEFAULT_PERIOD = 0    //min
        const val DEFAULT_DISTANCE = 0  //m
        const val DEFAULT_VOICE = true
        val Empty = SettingsDto(
            minInterval = DEFAULT_PERIOD,
            minDistance = DEFAULT_DISTANCE,
            voice = true
        )
    }
}