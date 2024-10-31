package com.cesoft.cesrunner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cesoft.cesrunner.data.local.dao.TrackDao
import com.cesoft.cesrunner.data.local.dao.TrackPointDao
import com.cesoft.cesrunner.domain.entity.TrackDto
import com.cesoft.cesrunner.domain.entity.TrackPointDto

@Database(
    entities = [
        TrackDto::class,
        TrackPointDto::class,
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun trackPointDao(): TrackPointDao
}
