package com.wiswm.nav.support.data.remote.http.response

import com.wiswm.nav.support.data.local.dataBase.entity.StompEntity
import kotlinx.serialization.Serializable

@Serializable
data class AppConfigResponse(
    //val DashCam: List<String> = emptyList(),
    // wrong response format from server! Server sends string and empty list in empty data case
    //val WeighingSystemInfo: List<String> = emptyList(),
    val Config: Config
) {
    /*
    fun getWeighingSystemInfoAsObject(): List<WeighingSystemInfo> {
        return Json.decodeFromString(WeighingSystemInfo[0])
    }
    */

    fun mapToStompEntity() =
        StompEntity(
            host = Config.Stomp.host,
            vhost = Config.Stomp.vhost,
            login = Config.Stomp.login,
            password = Config.Stomp.password
        )
}

@Serializable
data class WeighingSystemInfo(
    val deviceName: String,
    val address: String,
    val type: Int,
    val blacklistActive: Boolean,
    val scanningTimeout: Int,
    val lists17SymbolsFormat: Boolean
)

@Serializable
data class Config(
    val SecurityPass: String,
    val VolumeUnit: VolumeUnit,
    val PgDbName: String,
    val SpeedUnit: String,
    val Stomp: Stomp
)

@Serializable
data class VolumeUnit(
    val Name: String,
    val Value: String
)

@Serializable
data class Stomp(
    val host: String,
    val vhost: String,
    val login: String,
    val password: String
)