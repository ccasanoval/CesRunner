package com.cesoft.cesrunner.domain.entity

data class SettingsDto(
    val period: Int,
) {
    companion object {
        const val DEFAULT_PERIOD = 0   //min
        val Empty = SettingsDto(DEFAULT_PERIOD)
    }
}