package com.cesoft.cesrunner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cesoft.cesrunner.data.local.entity.TrackingDto
import com.cesoft.cesrunner.data.local.entity.TrackingTableName

@Dao
interface TrackingDao {
    @Query("SELECT * FROM $TrackingTableName")
    suspend fun getAll(): List<TrackingDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(tracking: TrackingDto)

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    //suspend fun addAll(list: List<TrackingDto>)

    //@Delete
    //suspend fun delete(tracking: TrackingDto)

    @Query("DELETE FROM $TrackingTableName")
    suspend fun deleteAll()
}
