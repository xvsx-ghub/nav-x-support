package com.wiswm.nav.support.data.local.dataBase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wiswm.nav.support.userInterface.screen.common.Item

@Entity(tableName = "DestinationEntity")
data class DestinationEntity(
    val remoteId: String,
    val description: String,
    val lat: String,
    val lng: String,
    val weighbridgeList: List<Weighbridge>,
    val weightUnit: String,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
) {
    fun mapToSpinnerItem() = Item(remoteId.toLong(), description)
}