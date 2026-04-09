package com.wiswm.nav.support.data.remote.stomp

data class StompConnectionDetails (
    val host: String,
    val vhost: String,
    val login: String,
    val password: String
)