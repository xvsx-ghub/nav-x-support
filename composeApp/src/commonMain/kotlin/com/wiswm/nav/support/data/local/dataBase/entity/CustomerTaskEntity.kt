package com.wiswm.nav.support.data.local.dataBase.entity

import androidx.room.*

@Entity(tableName = "CustomerTaskEntity")
data class CustomerTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val address: String,
    val barcode: String,
    val binId: String,
    val binType: String,
    val binTypeId: Int,
    var bulkyItemId: Int,
    var bulkyItemName: String,
    val chipCode: String,
    val customerId: String,
    val customerName: String,
    val customerRefId: String,
    val customerType: String,
    var isAdhocTask: Boolean,
    var isBlacklisted: String,
    var lat: Float,
    var lng: Float,
    val routeId: String,
    val secondChipCode: String,
    var signatureRequired: Boolean,
    var taskId: String,
    var taskStatus: Int,
    var taskType: String,
    val wasteType: String,
    val wasteTypeId: String
)