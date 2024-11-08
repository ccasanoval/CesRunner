package com.cesoft.cesrunner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cesoft.cesrunner.data.local.entity.LocalTrackPointDto
import com.cesoft.cesrunner.data.local.entity.TrackPointTableName
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackPointDao {
    //@Query("SELECT * FROM $TrackPointTableName")
    //suspend fun getAll(): List<LocalTrackPointDto>

    @Query("SELECT * FROM $TrackPointTableName WHERE idTrack = :id")
    suspend fun getByTrackId(id: Long): List<LocalTrackPointDto>

    @Query("SELECT * FROM $TrackPointTableName WHERE idTrack = :id")
    fun getFlowByTrackId(id: Long): Flow<List<LocalTrackPointDto>>

    @Query("SELECT * FROM $TrackPointTableName WHERE idTrack = :id ORDER BY id DESC LIMIT 1")
    suspend fun getLastByTrackId(id: Long): LocalTrackPointDto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(data: LocalTrackPointDto)

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    //suspend fun addAll(list: List<TrackingDto>)

    //@Delete
    //suspend fun delete(tracking: TrackingDto)

    @Query("DELETE FROM $TrackPointTableName WHERE idTrack = :id")
    suspend fun deleteByTrackId(id: Long)

    @Query("DELETE FROM $TrackPointTableName")
    suspend fun deleteAll()
}
