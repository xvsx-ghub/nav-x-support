package com.wiswm.nav.support.util

object Logger {
    fun d(tag: String, message: String) = logDebug(tag, message)
    fun e(tag: String, message: String, throwable: Throwable? = null) =
        logError(tag, message, throwable)
}

internal expect fun logDebug(tag: String, message: String)
internal expect fun logError(tag: String, message: String, throwable: Throwable?)