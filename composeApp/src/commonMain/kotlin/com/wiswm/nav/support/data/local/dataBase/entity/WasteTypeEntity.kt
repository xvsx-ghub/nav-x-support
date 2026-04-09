package com.wiswm.nav.support.data.local.dataBase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wiswm.nav.support.userInterface.screen.common.Item

@Entity(tableName = "WasteTypeEntity")
data class WasteTypeEntity(
    val remoteId: String,
    val description: String,
    val bulkyStatus: Boolean,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
) {
    fun mapToSpinnerItem() = Item(remoteId.toLong(), description)
}