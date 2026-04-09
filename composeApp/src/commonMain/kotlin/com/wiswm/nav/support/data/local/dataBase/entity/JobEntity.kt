package com.wiswm.nav.support.data.local.dataBase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "JobEntity")
data class JobEntity(
    val type: Type,
    val completedStatus: Boolean,
    val timestamp: String?,
    val weighingIn: Weighing?,
    val weighingOut: Weighing?,
    val weighingNet: Weighing?,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
){
    enum class Type{
        Unknown,In,Out,InAndOut,Net
    }
}