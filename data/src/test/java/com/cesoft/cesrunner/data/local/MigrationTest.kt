package com.cesoft.cesrunner.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class MigrationTest {

    @Test
    fun `migration 1 to 2 creates the index on track_point idTrack with the name room expects`() {
        val db: SupportSQLiteDatabase = mockk(relaxed = true)

        AppDatabase.MIGRATION_1_2.migrate(db)

        assertEquals(1, AppDatabase.MIGRATION_1_2.startVersion)
        assertEquals(2, AppDatabase.MIGRATION_1_2.endVersion)
        verify {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_track_point_idTrack` ON `track_point` (`idTrack`)")
        }
    }
}
