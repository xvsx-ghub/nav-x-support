package com.wiswm.nav.support.data.remote.http.response

import com.wiswm.nav.support.data.local.dataBase.entity.JobEntity
import com.wiswm.nav.support.data.local.dataBase.entity.Weighing
import kotlinx.serialization.Serializable

@Serializable
data class PendingWeighingListResponse(
    val Weighings: List<PendingWeighingResponse>? = null,
)

@Serializable
data class PendingWeighingResponse(
    val WeighingId: Int? = null,
    val WasteTypeId: Int? = null,
    val DestinationId: Int? = null,
    val WeighbridgeIdIn: Int? = null,
    val WeightIn: Int? = null,
    val TimestampIn: Long? = null
){
    fun mapToJobEntity() = JobEntity(
        weighingIn = Weighing(
            remoteId = WeighingId.toString(),
            destinationId = DestinationId.toString(),
            weighbridgeId = WeighbridgeIdIn.toString(),
            wasteTypeId = WasteTypeId.toString(),
            weightValue = WeightIn.toString(),
            timestamp = TimestampIn.toString(), //? check format
            type = Weighing.Type.Automatic,
            direction = Weighing.Direction.In,
            remoteErrorMessage = null,
            docketPhoto = null
        ),
        type = JobEntity.Type.InAndOut,
        completedStatus = false,
        timestamp = TimestampIn.toString(), //? check format
        weighingOut = null,
        weighingNet = null,
    )
}