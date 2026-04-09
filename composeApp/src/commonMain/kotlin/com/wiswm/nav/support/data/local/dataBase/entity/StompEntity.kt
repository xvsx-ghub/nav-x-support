package com.wiswm.nav.support.data.local.dataBase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wiswm.nav.support.data.remote.stomp.StompConnectionDetails

@Entity(tableName = "StompEntity")
data class StompEntity(
    @PrimaryKey() val id: Int = 0,
    val host: String,
    val vhost: String,
    val login: String,
    val password: String
){
    fun mapToStompConnectionDetails() =
        StompConnectionDetails(
            host = host,
            vhost = vhost,
            login = login,
            password = password
        )
}