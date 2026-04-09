package com.wiswm.drawing_pad

import android.os.Build
import java.io.File
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

actual class System actual constructor() {
    @OptIn(ExperimentalTime::class)
    actual fun getCurrentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }

    actual fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            model = Build.MODEL ?: "Unknown",
            osVersion = Build.VERSION.RELEASE ?: "Unknown"
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