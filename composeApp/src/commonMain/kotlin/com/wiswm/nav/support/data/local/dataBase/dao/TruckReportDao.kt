package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wiswm.nav.support.data.local.dataBase.entity.TruckReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TruckReportDao {
    @Query("DELETE FROM TruckReportEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TruckReportEntity): Long

    @Update
    suspend fun update(item: TruckReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<TruckReportEntity>)

    @Query("SELECT * FROM TruckReportEntity")
    fun getAllAsFlow(): Flow<List<TruckReportEntity>>

    @Query("SELECT * FROM TruckReportEntity")
    suspend fun getAll(): List<TruckReportEntity>?
}