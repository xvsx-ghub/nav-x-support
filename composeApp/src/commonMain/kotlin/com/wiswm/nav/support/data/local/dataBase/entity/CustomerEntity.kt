package com.wiswm.nav.support.data.local.dataBase.entity

import androidx.room.*

@Entity(tableName = "CustomerEntity")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId:Int,
    val customerRefId:String,
    val customerType :Int,
    val name :String,
    val notes :String,
    val gateCode: String,
    val address: String,
    val phone: String,
    val lat: Double,
    val lng: Double,
    val isBlacklisted: Boolean,
    val hasTickets: Int,
    val isCollected: Boolean,
    val isMissed: Boolean,
    val signatureRequired:Int,
    val isNew: Boolean,
    val walkUpLng: String,
    val walkUpLat: String,
    val collectionLocationLat: Double,
    val collectionLocationLng: Double,
    val projectionPointLat: Double,
    val projectionPointLng: Double,
    val position: Int
)