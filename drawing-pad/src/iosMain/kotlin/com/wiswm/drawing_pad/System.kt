package com.wiswm.drawing_pad

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.free
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.create
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIDevice
import platform.posix.memcpy

actual class System actual constructor() {
    actual fun getCurrentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    actual fun getDeviceInfo(): DeviceInfo {
        val device = UIDevice.currentDevice
        return DeviceInfo(
            model = device.model ?: "Unknown",
            osVersion = device.systemVersion ?: "Unknown"
        )
    }

    @OptIn(BetaInteropApi::class)
    actual suspend fun loadBytesFromPath(path: String): ByteArray? {
        val fileManager = NSFileManager.defaultManager
        val fileExists = fileManager.fileExistsAtPath(path)

        if (!fileExists) {
            return null
        }

        val nsData = NSData.create(contentsOfFile = path)
        return nsData?.toByteArray() ?: ByteArray(0)
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray {
        val lengthInt = length.toInt()
        val bytes = ByteArray(lengthInt)
        memScoped {
            val pointer = bytes.refTo(0).getPointer(this)
            memcpy(pointer, this@toByteArray.bytes, lengthInt.convert())
        }
        return bytes
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun deleteFile(filePath: String?): Boolean {
        filePath?.let{
            val fileManager = NSFileManager.defaultManager
            val errorPtr = nativeHeap.alloc<ObjCObjectVar<NSError?>>()

            val success = fileManager.removeItemAtPath(it, errorPtr.ptr)
            if (!success) {
                val error = errorPtr.value
                println("Failed to delete file: ${error?.localizedDescription}")
            }

            nativeHeap.free(errorPtr)
            return success
        }
        return false
    }
}