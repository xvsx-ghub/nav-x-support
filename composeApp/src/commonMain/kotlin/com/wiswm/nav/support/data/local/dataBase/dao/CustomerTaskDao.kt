package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wiswm.nav.support.data.local.dataBase.entity.CustomerTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CustomerTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CustomerTaskEntity>)

    @Update
    suspend fun update(item: CustomerTaskEntity)

    @Query("SELECT * FROM CustomerTaskEntity")
    suspend fun getAll(): List<CustomerTaskEntity>

    @Query("SELECT * FROM CustomerTaskEntity WHERE customerId = :customerId")
    suspend fun getByCustomerId(customerId: String): List<CustomerTaskEntity>

    @Query("SELECT * FROM CustomerTaskEntity WHERE customerRefId = :customerRefId")
    suspend fun getByCustomerRefId(customerRefId: String): List<CustomerTaskEntity>

    @Query("SELECT * FROM CustomerTaskEntity")
    fun getAllAsFlow(): Flow<List<CustomerTaskEntity>>

    @Query("SELECT * FROM CustomerTaskEntity WHERE customerId = :customerId")
    fun getByCustomerIdAsFlow(customerId: String): Flow<List<CustomerTaskEntity>>

    @Query("SELECT * FROM CustomerTaskEntity WHERE customerRefId = :customerRefId")
    fun getByCustomerRefIdAsFlow(customerRefId: String): Flow<List<CustomerTaskEntity>>

    @Query("DELETE FROM CustomerTaskEntity")
    suspend fun clear()

    @Delete
    suspend fun delete(item: CustomerTaskEntity)

    @Query("SELECT COUNT(*) FROM CustomerTaskEntity")
    suspend fun countAll(): Int
}