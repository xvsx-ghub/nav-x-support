package com.wiswm.nav.support.data.remote.http.response

import com.wiswm.nav.support.data.local.dataBase.entity.AuthorizationEntity
import kotlinx.serialization.Serializable

@Serializable
data class AuthorizationResponse(
    val RosterId: Int,
    val route_id: Int,
    val route_name: String,
    val sessionKey: String,
    val UserId: String,
    val AgentType: String,
    val name: String,
    val UserName: String,
    val mq: String,
    val SecurityPass: String
) {
    fun mapToAuthorizationEntity(truckId: Int, truckReg: String, truckUserId: String, truckHash: String) =
        AuthorizationEntity(
            rosterId = RosterId,
            routeId = route_id,
            routeName = route_name,
            sessionKey = sessionKey,
            userId = UserId,
            agentType = AgentType,
            name = name,
            userName = UserName,
            mq = mq,
            securityPass = SecurityPass,
            truckId = truckId,
            truckReg = truckReg,
            truckUserId = truckUserId,
            truckHash = truckHash
        )
}
