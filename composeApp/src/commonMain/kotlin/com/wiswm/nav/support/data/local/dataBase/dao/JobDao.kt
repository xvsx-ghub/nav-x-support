package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wiswm.nav.support.data.local.dataBase.entity.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("DELETE FROM JobEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: JobEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(itemList: List<JobEntity>)

    @Update
    suspend fun update(item: JobEntity)

    @Delete
    suspend fun delete(item: JobEntity)

    @Query("SELECT COUNT(*) FROM JobEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM JobEntity")
    fun getAllAsFlow(): Flow<List<JobEntity>>

    @Query("SELECT * FROM JobEntity")
    suspend fun getAll(): List<JobEntity>

    @Query("SELECT * FROM JobEntity WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): JobEntity?
}