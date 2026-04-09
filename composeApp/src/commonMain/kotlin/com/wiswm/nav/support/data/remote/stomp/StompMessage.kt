package com.wiswm.nav.support.data.remote.stomp

data class StompMessage(
    val subscriberTag: String,
    val message: String
){
    companion object {
        const val NAV_X_MESSAGE_SIGNATURE = "WIS"
        const val SEPARATOR = ":;:"
    }
}