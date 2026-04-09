package com.wiswm.nav.support.data.local.dataBase.entity

import com.wiswm.nav.support.userInterface.screen.common.Item
import kotlinx.serialization.Serializable

@Serializable
data class Weighbridge(
    val remoteId: String,
    val name: String,
    val type: String,
    val canWeighStatus: Boolean,
    val weightUnit: String,
) {
    fun mapToSpinnerItem() = Item(remoteId.toLong(), name)
}