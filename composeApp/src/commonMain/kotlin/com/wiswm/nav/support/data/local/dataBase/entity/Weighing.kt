package com.wiswm.nav.support.data.local.dataBase.entity

import kotlinx.serialization.Serializable

@Serializable
data class Weighing(
    val remoteId: String?,
    val destinationId: String?,
    val weighbridgeId: String?,
    val wasteTypeId: String?,
    val weightValue: String?,
    val timestamp: String?,
    val type: Type?,
    val direction: Direction?,
    val remoteErrorMessage: String?,
    val docketPhoto: String?
) {
    enum class Type {
        Unknown,
        Manual,
        Automatic
    }

    enum class Direction {
        Unknown,
        In,
        Out,
        Net
    }
}