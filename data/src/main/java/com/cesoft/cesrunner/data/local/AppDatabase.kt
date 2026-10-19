package com.cesoft.cesrunner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cesoft.cesrunner.data.local.dao.TrackDao
import com.cesoft.cesrunner.data.local.dao.TrackPointDao
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
import com.cesoft.cesrunner.data.local.entity.TrackPointTableName

@Database(
    entities = [
        LocalTrackDto::class,
        LocalTrackPointDto::class,
    ],
    version = 2
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun trackPointDao(): TrackPointDao

    companion object {
        private const val NAME = "CesRunner"

        /// v2: index on track_point.idTrack, every track query filters by it
        val MIGRATION_1_2 = object: Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_${TrackPointTableName}_idTrack`" +
                    " ON `$TrackPointTableName` (`idTrack`)"
                )
            }
        }

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, NAME)
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
