package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wiswm.nav.support.data.local.dataBase.entity.RequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestDao {
    @Query("DELETE FROM RequestEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RequestEntity)

    @Update
    suspend fun update(item: RequestEntity)

    @Delete
    suspend fun delete(item: RequestEntity)

    @Query("SELECT COUNT(*) FROM RequestEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM RequestEntity")
    fun getAsFlow(): Flow<List<RequestEntity>>

    @Query("SELECT * FROM RequestEntity")
    suspend fun get(): List<RequestEntity>

    @Query("SELECT * FROM RequestEntity WHERE id = :id LIMIT 1")
    suspend fun get(id: Int): RequestEntity?

    @Query("SELECT * FROM RequestEntity ORDER BY id DESC LIMIT 1")
    suspend fun getLast(): RequestEntity?
}