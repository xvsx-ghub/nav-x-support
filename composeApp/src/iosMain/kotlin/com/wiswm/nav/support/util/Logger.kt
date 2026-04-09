package com.wiswm.nav.support.util

internal actual fun logDebug(tag: String, message: String) {
    println("DEBUG [$tag]: $message")
}

internal actual fun logError(tag: String, message: String, throwable: Throwable?) {
    println("ERROR [$tag]: $message")
    throwable?.printStackTrace()
}