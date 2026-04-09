package com.wiswm.nav.support.data.remote.stomp.subscriber

import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders

class RouteSubscriber(
    val truckId: String,
    val deviceId: String
) {
    private val exchange = "wis.direct"
    private val routingKey = "request_route_data.$truckId"
    val destination = "/exchange/$exchange/$routingKey"
    val headers = StompSubscribeHeaders(destination) {
        this["durable"] = "true"
        this["auto-delete"] = "false"
        this["ack"] = "client-individual"
        this["x-queue-name"] = "$routingKey.$deviceId"
    }

    companion object {
        const val TAG = "RouteSubscriber"
    }
}