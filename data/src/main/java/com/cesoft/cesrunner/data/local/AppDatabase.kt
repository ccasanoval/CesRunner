package com.cesoft.cesrunner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cesoft.cesrunner.data.local.dao.TrackDao
import com.cesoft.cesrunner.data.local.dao.TrackPointDao
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto

@Database(
    entities = [
        LocalTrackDto::class,
        LocalTrackPointDto::class,
    ],
    version = 1
)
//@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun trackPointDao(): TrackPointDao
}
