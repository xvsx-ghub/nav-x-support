package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wiswm.nav.support.data.local.dataBase.entity.AuthorizationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorizationDao {

    @Query("DELETE FROM AuthorizationEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: AuthorizationEntity)

    @Query("SELECT COUNT(*) FROM AuthorizationEntity")
    suspend fun count(): Int

    @Query("SELECT * FROM AuthorizationEntity LIMIT 1")
    fun getAsFlow(): Flow<AuthorizationEntity?>

    @Query("SELECT * FROM AuthorizationEntity LIMIT 1")
    suspend fun get(): AuthorizationEntity?
}