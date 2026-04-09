package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wiswm.nav.support.data.local.dataBase.entity.NotServicingReasonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotServicingReasonDao {
    @Query("DELETE FROM NotServicingReasonEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: NotServicingReasonEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<NotServicingReasonEntity>)

    @Query("SELECT * FROM NotServicingReasonEntity")
    fun getAllAsFlow(): Flow<List<NotServicingReasonEntity>>

    @Query("SELECT * FROM NotServicingReasonEntity")
    suspend fun getAll(): List<NotServicingReasonEntity>?
}