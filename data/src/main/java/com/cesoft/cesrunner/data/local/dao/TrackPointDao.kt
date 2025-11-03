package com.cesoft.cesrunner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cesoft.cesrunner.data.local.entity.LocalLocationDto
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
import com.cesoft.cesrunner.data.local.entity.TrackPointTableName
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackPointDao {

    @Query("SELECT * FROM $TrackPointTableName WHERE idTrack = :id")
    suspend fun getByTrackId(id: Long): List<LocalTrackPointDto>

    @Query("SELECT * FROM $TrackPointTableName WHERE idTrack = :id")
    fun getFlowByTrackId(id: Long): Flow<List<LocalTrackPointDto>>

    @Query("SELECT * FROM $TrackPointTableName WHERE idTrack = :id ORDER BY id DESC LIMIT 1")
    suspend fun getLastByTrackId(id: Long): LocalTrackPointDto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(data: LocalTrackPointDto)

    @Query("DELETE FROM $TrackPointTableName WHERE idTrack = :id")
    suspend fun deleteByTrackId(id: Long)

    @Query("DELETE FROM $TrackPointTableName")
    suspend fun deleteAll()

    // IA AGENT
    @Query("SELECT id, idTrack, latitude, longitude FROM $TrackPointTableName")
    suspend fun getAllLocations(): List<LocalLocationDto>
}
