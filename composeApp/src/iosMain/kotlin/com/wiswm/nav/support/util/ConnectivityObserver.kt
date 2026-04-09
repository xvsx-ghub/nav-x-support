package com.wiswm.nav.support.util

import platform.Network.*
import platform.darwin.*

actual class ConnectivityObserver actual constructor() {
    companion object {
        const val TAG = "AndroidConnectivityObserver"
    }

    actual fun create(onConnectionStateChanged: (onlineStatus: Boolean) -> Unit) {
        val monitor: nw_path_monitor_t = nw_path_monitor_create()

        nw_path_monitor_set_update_handler(monitor) { path: nw_path_t? ->
            val status = path?.let {
                nw_path_get_status(it) == nw_path_status_satisfied
            } ?: false

            onConnectionStateChanged(status)

            val queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)
            nw_path_monitor_set_queue(monitor, queue)
            nw_path_monitor_start(monitor)
        }
    }
}