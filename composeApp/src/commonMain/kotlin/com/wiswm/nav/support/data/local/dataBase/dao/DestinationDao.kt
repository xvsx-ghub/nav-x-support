package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wiswm.nav.support.data.local.dataBase.entity.DestinationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DestinationDao {
    @Query("DELETE FROM DestinationEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DestinationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(item: List<DestinationEntity>)

    @Update
    suspend fun update(item: DestinationEntity)

    @Delete
    suspend fun delete(item: DestinationEntity)

    @Query("SELECT COUNT(*) FROM DestinationEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM DestinationEntity")
    fun getAllAsFlow(): Flow<List<DestinationEntity>>

    @Query("SELECT * FROM DestinationEntity")
    suspend fun getAll(): List<DestinationEntity>

    @Query("SELECT * FROM DestinationEntity WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DestinationEntity?
}