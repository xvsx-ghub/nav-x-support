package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wiswm.nav.support.data.local.dataBase.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("DELETE FROM TaskEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TaskEntity): Long

    @Update
    suspend fun update(item: TaskEntity)

    @Delete
    suspend fun delete(item: TaskEntity)

    @Query("SELECT COUNT(*) FROM TaskEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM TaskEntity")
    fun getAsFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM TaskEntity")
    suspend fun get(): List<TaskEntity>

    @Query("SELECT * FROM TaskEntity WHERE id = :taskId LIMIT 1")
    suspend fun get(taskId: Long): TaskEntity?

    @Query("SELECT * FROM TaskEntity WHERE filePath = :imageFilePath LIMIT 1")
    suspend fun getByImageFilePath(imageFilePath: String): TaskEntity?

    @Query("SELECT * FROM TaskEntity WHERE customerId = :customerId")
    suspend fun getByCustomerId(customerId: String): List<TaskEntity>?
}