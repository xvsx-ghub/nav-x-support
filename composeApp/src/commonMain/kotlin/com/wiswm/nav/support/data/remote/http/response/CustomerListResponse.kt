package com.wiswm.nav.support.data.remote.http.response

import com.wiswm.nav.support.data.local.dataBase.entity.CustomerEntity
import kotlinx.serialization.Serializable

@Serializable
data class CustomerListResponse(
    val customers: List<CustomerResponse>? = null
)

@Serializable
data class CustomerResponse(
    val customerId: Int,
    val customerRefId: String,
    val customerType: Int,
    val name: String,
    val notes: String,
    val gateCode: String,
    val address: String,
    val phone: String,
    val lat: Double,
    val lng: Double,
    val isBlacklisted: Boolean,
    val hasTickets: Int,
    val isCollected: Boolean,
    val isMissed: Boolean,
    val signatureRequired: Int,
    val isNew: Boolean,
    val walkUpLng: String,
    val walkUpLat: String,
    val collectionLocationLat: Double,
    val collectionLocationLng: Double,
    val projectionPointLat: Double,
    val projectionPointLng: Double,
    val position: Int
) {
    fun mapToCustomerEntity() = CustomerEntity(
        customerId = customerId,
        customerRefId = customerRefId,
        customerType = customerType,
        name = name,
        notes = notes,
        gateCode = gateCode,
        address = address,
        phone = phone,
        lat = lat,
        lng = lng,
        isBlacklisted = isBlacklisted,
        hasTickets = hasTickets,
        isCollected = isCollected,
        isMissed = isMissed,
        signatureRequired = signatureRequired,
        isNew = isNew,
        walkUpLng = walkUpLng,
        walkUpLat = walkUpLat,
        collectionLocationLat = collectionLocationLat,
        collectionLocationLng = collectionLocationLng,
        projectionPointLat = projectionPointLat,
        projectionPointLng = projectionPointLng,
        position = position
    )
}