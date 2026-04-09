package com.wiswm.nav.support.data.local.dataBase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TaskEntity")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: Int,
    val customerId: String,
    val customerRefId: String,
    val liftId: String,
    val deviceId: String,
    val timestamp: String,
    val reasonId: String,
    var status: Int = 1,
    var filePath: String = ""
){
    companion object{
        const val TAKE_SIGNATURE_TYPE = 1
        const val TAKE_PHOTO_TYPE = 2
        const val NEW_STATUS = 1
        const val PENDING_STATUS = 2
        const val DONE_STATUS = 3
    }
}