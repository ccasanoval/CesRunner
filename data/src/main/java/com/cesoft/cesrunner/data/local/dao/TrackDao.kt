package com.cesoft.cesrunner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.TrackPointTableName
import com.cesoft.cesrunner.data.local.entity.TrackTableName

@Dao
interface TrackDao {
    @Query("SELECT * FROM $TrackTableName")
    suspend fun getAll(): List<LocalTrackDto>

    @Query("SELECT * FROM $TrackTableName WHERE id = :id")
    suspend fun getById(id: Long): LocalTrackDto

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(data: LocalTrackDto)

    @Delete
    suspend fun delete(data: LocalTrackDto)

    @Query("DELETE FROM $TrackPointTableName")
    suspend fun deleteAll()
}
