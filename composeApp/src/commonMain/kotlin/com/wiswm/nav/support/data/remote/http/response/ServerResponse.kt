package com.wiswm.nav.support.data.remote.http.response

import kotlinx.serialization.Serializable

@Serializable
data class ServerResponse(
    val name: String,
    val url: String
)
