package com.wiswm.nav.support.data.remote.http.response

import com.wiswm.nav.support.data.local.dataBase.entity.NotServicingReasonEntity
import kotlinx.serialization.Serializable

@Serializable
data class NotServicingReasonListResponse(
    val BinNotServicingReasons: List<NotServicingReasonResponse>
)

@Serializable
data class NotServicingReasonResponse(
    val Key: String,
    val Description: String
){
    fun mapToNotServicingReasonEntity() = NotServicingReasonEntity(
        key = Key,
        description = Description
    )
}