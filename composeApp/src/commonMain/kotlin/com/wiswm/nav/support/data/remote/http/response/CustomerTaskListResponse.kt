package com.wiswm.nav.support.data.remote.http.response

import com.wiswm.nav.support.data.local.dataBase.entity.CustomerTaskEntity
import kotlinx.serialization.Serializable

@Serializable
data class CustomerTaskListResponse(
    val tasks: List<CustomerTaskResponse>? = null
)

@Serializable
data class CustomerTaskResponse(
    val address: String?,
    val barcode: String?,
    val binId: String?,
    val binType: String?,
    val binTypeId: Int?,
    var bulkyItemId: Int?,
    var bulkyItemName: String?,
    val chipCode: String?,
    val customerId: String?,
    val customerName: String?,
    val customerRefId: String?,
    val customerType: String?,
    var isAdhocTask: Boolean?,
    var isBlacklisted: String?,
    var lat: Float?,
    var lng: Float?,
    val routeId: String?,
    val secondChipCode: String?,
    var signatureRequired: Boolean?,
    var taskId: String?,
    var taskStatus: Int?,
    var taskType: String?,
    val wasteType: String?,
    val wasteTypeId: String?
) {
    fun mapToCustomerTaskEntity(): CustomerTaskEntity = CustomerTaskEntity(
        address = address ?: "",
        barcode = barcode ?: "",
        binId = binId ?: "",
        binType = binType?: "",
        binTypeId = binTypeId?: -1,
        bulkyItemId = bulkyItemId?: -1,
        bulkyItemName = bulkyItemName?: "",
        chipCode = chipCode?: "",
        customerId = customerId?: "",
        customerName = customerName?: "",
        customerRefId = customerRefId?: "",
        customerType = customerType?: "",
        isAdhocTask = isAdhocTask?: false,
        isBlacklisted = isBlacklisted?: "",
        lat = lat?: 0F,
        lng = lng?: 0F,
        routeId = routeId?: "",
        secondChipCode = secondChipCode?: "",
        signatureRequired = signatureRequired?: false,
        taskId = taskId?: "",
        taskStatus = taskStatus?: -1,
        taskType = taskType?: "",
        wasteType = wasteType?: "",
        wasteTypeId = wasteTypeId?: ""
    )
}