package com.wiswm.nav.support.data.local.dataBase.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wiswm.nav.support.data.local.dataBase.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("DELETE FROM CustomerEntity")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CustomerEntity>)

    @Update
    suspend fun update(item: CustomerEntity)

    @Delete
    suspend fun delete(item: CustomerEntity)

    @Query("SELECT COUNT(*) FROM CustomerEntity")
    suspend fun countAll(): Int

    @Query("SELECT * FROM CustomerEntity")
    fun getAllAsFlow(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM CustomerEntity")
    suspend fun getAll(): List<CustomerEntity>?

    @Query("SELECT * FROM CustomerEntity WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): CustomerEntity?

    @Query("SELECT * FROM CustomerEntity WHERE customerId = :customerId LIMIT 1")
    suspend fun getByCustomerId(customerId: Int): CustomerEntity?

    @Query("SELECT * FROM CustomerEntity WHERE customerId LIKE '%' || :customerId || '%'")
    suspend fun getSimilarByCustomerId(customerId: Int): List<CustomerEntity>?

    @Query("SELECT * FROM CustomerEntity WHERE customerRefId LIKE '%' || :customerRefId || '%'")
    suspend fun getSimilarByCustomerRefId(customerRefId: Int): List<CustomerEntity>?

    @Query("SELECT * FROM CustomerEntity WHERE address LIKE '%' || :address || '%'")
    suspend fun getSimilarByAddress(address: String): List<CustomerEntity>?
}