package com.cesoft.cesrunner.domain.entity

import android.location.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationDto(
    val latitude: Double,
    val longitude: Double,
) {
    override fun toString(): String {
        return "LocationDto { latitude: $latitude, longiturde: $longitude }"
    }
    fun distanceTo(location: LocationDto) = calculationByDistance(location, this)
    //fun distanceTo(lat: Double, lng: Double) = calculationByDistance(LocationDto(lat,lng), this)
    companion object {
        val Empty = LocationDto(0.0, 0.0)
        fun fromLocation(location: Location) = LocationDto(location.latitude, location.longitude)
        private fun calculationByDistance(
            org: LocationDto,
            des: LocationDto,
        ): Double {
            val diffLat = toRadians(des.latitude - org.latitude)
            val diffLon = toRadians(des.longitude - org.longitude)
            val orgLatRad = toRadians(org.latitude)
            val desLatRad = toRadians(des.latitude)
            val a = sin(diffLat / 2) * sin(diffLat / 2) +
                    sin(diffLon / 2) * sin(diffLon / 2) * cos(orgLatRad) * cos(desLatRad)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return R * c
        }
        private const val R = 6371000 // m (Earth radius)
        private fun toRadians(deg: Double): Double {
            return deg * (Math.PI / 180)
        }
    }
}