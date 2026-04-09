package com.wiswm.drawing_pad

data class DeviceInfo(
    val model: String,
    val osVersion: String
)

expect class System() {
    fun getCurrentTimeMillis(): Long
    fun getDeviceInfo(): DeviceInfo
    suspend fun loadBytesFromPath(path: String): ByteArray?
    fun deleteFile(filePath: String?): Boolean
}
