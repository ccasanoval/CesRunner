package com.cesoft.cesrunner.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.cesoft.cesrunner.data.local.entity.LocalTrackDto
import com.cesoft.cesrunner.data.local.entity.TrackTableName

@Dao
interface TrackDao {
    @Query("SELECT * FROM $TrackTableName")
    suspend fun getAll(): List<LocalTrackDto>

    @Query("SELECT * FROM $TrackTableName WHERE id = :id")
    suspend fun getById(id: Long): LocalTrackDto

    @Query("SELECT * FROM $TrackTableName ORDER BY id DESC LIMIT 1")
    suspend fun getLast(): LocalTrackDto

    @Upsert
    suspend fun create(data: LocalTrackDto): Long

    @Update
    suspend fun update(data: LocalTrackDto)

    @Delete
    suspend fun delete(data: LocalTrackDto)

    @Query("DELETE FROM $TrackTableName")
    suspend fun deleteAll()
}
