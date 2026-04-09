package com.wiswm.nav.support.util

import android.util.Log

internal actual fun logDebug(tag: String, message: String) {
    Log.d(tag, message)
}

internal actual fun logError(tag: String, message: String, throwable: Throwable?) {
    Log.e(tag, message, throwable)
}