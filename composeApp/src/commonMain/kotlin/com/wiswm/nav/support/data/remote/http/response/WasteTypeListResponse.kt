package com.wiswm.nav.support.data.remote.http.response

import com.wiswm.nav.support.data.local.dataBase.entity.WasteTypeEntity
import kotlinx.serialization.Serializable

@Serializable
data class WasteTypeListResponse(
    val WasteTypes: List<WasteTypeResponse>? = null
)

@Serializable
data class WasteTypeResponse(
    val Id: String,
    val Description: String,
    val IsBulky: Boolean,
) {
    fun mapToWasteTypeEntity() = WasteTypeEntity(Id, Description, IsBulky)
}