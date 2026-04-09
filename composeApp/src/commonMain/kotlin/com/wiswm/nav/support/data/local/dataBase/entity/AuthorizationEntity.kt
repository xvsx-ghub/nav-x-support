package com.wiswm.nav.support.data.local.dataBase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AuthorizationEntity")
data class AuthorizationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rosterId: Int,
    val routeId: Int,
    val routeName: String,
    val sessionKey: String,
    val userId: String,
    val agentType: String,
    val name: String,
    val userName: String,
    val mq: String,
    val securityPass: String,
    val truckId: Int,
    val truckReg: String,
    val truckUserId: String,
    val truckHash: String
)