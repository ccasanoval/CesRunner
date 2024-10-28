package com.cesoft.cesrunner.domain.entity

data class CurrentTrackingDto(
    val isTracking: Boolean,
) {
    companion object {
        val Empty = CurrentTrackingDto(false)
    }
}