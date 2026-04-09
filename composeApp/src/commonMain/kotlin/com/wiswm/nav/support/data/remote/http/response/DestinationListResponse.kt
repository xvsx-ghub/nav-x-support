package com.wiswm.nav.support.data.remote.http.response

import com.wiswm.nav.support.data.local.dataBase.entity.DestinationEntity
import com.wiswm.nav.support.data.local.dataBase.entity.Weighbridge
import kotlinx.serialization.Serializable

@Serializable
data class DestinationListResponse(
    val destinationDescriptions: List<DestinationResponse>? = null
)

@Serializable
data class DestinationResponse(
    val Id: String,
    val Description: String,
    val Lat: String?,
    val Long: String?,
    val Weighbridges: List<WeighbridgeResponse>,
    val weight_unit: String
) {
    fun mapToDestinationEntity() = DestinationEntity(
        Id,
        Description,
        Lat ?: "",
        Long ?: "",
        Weighbridges.map { weighbridge -> weighbridge.mapToWeighbridgeEntity() },
        weight_unit
    )
}

@Serializable
data class WeighbridgeResponse(
    val Id: String,
    val Name: String,
    val Type: String,
    val CanWeigh: Boolean,
    val weight_unit: String
) {
    companion object{
        const val IN = "In only"
        const val OUT = "Out only"
        const val IN_AND_OUT = "In & Out"
    }
    fun mapToWeighbridgeEntity() = Weighbridge(
        Id,
        Name,
        Type,
        CanWeigh,
        weight_unit
    )
}