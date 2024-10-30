package com.cesoft.cesrunner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

const val TrackingTableName = "tracking"
@Entity(tableName = TrackingTableName)
data class TrackingDto(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val date: Long,
    val latitude: Double,
    val longitude: Double,
)