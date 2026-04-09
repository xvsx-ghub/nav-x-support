package com.wiswm.nav.support.util

import android.os.Build
import com.wiswm.nav.support.BuildConfig
import java.io.File
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

actual class System actual constructor() {
    actual fun getAppVersion(): String {
        return BuildConfig.VERSION_NAME
    }

    @OptIn(ExperimentalTime::class)
    actual fun getCurrentTimeSeconds(): Long {
        return Clock.System.now().epochSeconds
    }

    actual fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            model = Build.MODEL ?: "Unknown",
            osVersion = Build.VERSION.RELEASE ?: "Unknown",
            id = Build.ID ?: "Unknown"
        )
    }

    actual suspend fun loadBytesFromPath(path: String): ByteArray? {
        val file = File(path)
        return if(file.exists()) file.readBytes()
        else null
    }

    actual fun deleteFile(filePath: String?): Boolean {
        if (filePath.isNullOrEmpty()) return false
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}