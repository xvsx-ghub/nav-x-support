package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wiswm.nav.support.data.local.dataBase.entity.StompEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StompDao {
    @Query("DELETE FROM StompEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: StompEntity)

    @Query("SELECT COUNT(*) FROM StompEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM StompEntity LIMIT 1")
    fun getAsFlow(): Flow<StompEntity?>

    @Query("SELECT * FROM StompEntity LIMIT 1")
    suspend fun get(): StompEntity?
}