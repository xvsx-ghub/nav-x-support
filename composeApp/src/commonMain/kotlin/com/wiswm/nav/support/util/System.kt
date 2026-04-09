package com.wiswm.nav.support.util

data class DeviceInfo(
    val model: String,
    val osVersion: String,
    val id: String
)

expect class System() {
    fun getAppVersion(): String
    fun getCurrentTimeSeconds(): Long
    fun getDeviceInfo(): DeviceInfo
    suspend fun loadBytesFromPath(path: String): ByteArray?
    fun deleteFile(filePath: String?): Boolean
}
