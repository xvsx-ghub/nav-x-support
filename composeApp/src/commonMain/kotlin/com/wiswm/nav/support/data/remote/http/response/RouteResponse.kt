package com.wiswm.nav.support.data.remote.http.response

import kotlinx.serialization.Serializable

@Serializable
data class RouteResponse(
    val RouteData: Route? = null
)

@Serializable
data class Route(
    val TruckRosterId: Int = -1,
    val RouteId: Int = -1,
    val RouteName: String,
    val RouteType: Int = -1,
    val OptimizationVersion: Int = -1,
    val Directions: List<DirectionResponse>? = emptyList(),
    val DirectionsHash: String = ""
)

@Serializable
class DirectionResponse(
    val id: Long? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val bearingToNext: Int? = null,
    val distanceToNextPoint: Int? = null,
    val numberOfPointsToManeuver: Int? = null,
    val distanceToNextManeuver: Int? = null,
    val isManeuver: Boolean = false,
    val maneuverType: Int? = null,
    val roundaboutExit: Int? = null,
    val type: Int? = null,
    val backward: Boolean = false,
    val isPassed: Boolean = false,
)