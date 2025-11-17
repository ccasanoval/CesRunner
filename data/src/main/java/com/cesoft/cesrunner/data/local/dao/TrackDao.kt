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
    @Query("SELECT * FROM $TrackTableName ORDER BY id DESC")
    suspend fun getAll(): List<LocalTrackDto>

    @Query("SELECT * FROM $TrackTableName WHERE id = :id")
    suspend fun getById(id: Long): LocalTrackDto?

    @Query("SELECT * FROM $TrackTableName ORDER BY id DESC LIMIT 1")
    suspend fun getLast(): LocalTrackDto?

    @Upsert
    suspend fun create(data: LocalTrackDto): Long

    @Update
    suspend fun update(data: LocalTrackDto)

    @Delete
    suspend fun delete(data: LocalTrackDto)

    @Query("DELETE FROM $TrackTableName WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM $TrackTableName")
    suspend fun deleteAll()

    // AI AGENT
    @Query("SELECT * FROM $TrackTableName " +
            " WHERE (:id IS NULL OR id = :id)" +
            " AND (:name IS NULL OR name LIKE '%' || :name || '%')" +
            //" AND (:name IS NULL OR name LIKE :name)" +
            " AND (:distance IS NULL OR (distance < :distance +100 AND distance > :distance -100))" +
            " AND (:dateIni IS NULL OR :dateEnd IS NULL OR (:dateIni < timeIni AND :dateEnd > timeEnd))" +
            " ORDER BY id"
    )
    suspend fun filter(
        id: Long? = null,
        name: String? = null,
        distance: Int? = null,
        dateIni: String? = null,
        dateEnd: String? = null,
    ): List<LocalTrackDto>
}
