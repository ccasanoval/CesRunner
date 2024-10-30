package com.cesoft.cesrunner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cesoft.cesrunner.data.local.dao.TrackingDao
import com.cesoft.cesrunner.data.local.entity.TrackingDto

@Database(
    entities = [
        TrackingDto::class,
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackingDao(): TrackingDao
}
