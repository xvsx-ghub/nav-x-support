package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wiswm.nav.support.data.local.dataBase.entity.WasteTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteTypeDao {
    @Query("DELETE FROM WasteTypeEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WasteTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(item: List<WasteTypeEntity>)

    @Update
    suspend fun update(item: WasteTypeEntity)

    @Delete
    suspend fun delete(item: WasteTypeEntity)

    @Query("SELECT COUNT(*) FROM WasteTypeEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM WasteTypeEntity")
    fun getAllAsFlow(): Flow<List<WasteTypeEntity>>

    @Query("SELECT * FROM WasteTypeEntity")
    suspend fun getAll(): List<WasteTypeEntity>

    @Query("SELECT * FROM WasteTypeEntity WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WasteTypeEntity?
}