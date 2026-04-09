package com.wiswm.nav.support.data.remote.http.response

import kotlinx.serialization.Serializable

@Serializable
data class WeightResponse(
    val current_weight: Int? = null,
    val stable_weight: Int? = null
)
