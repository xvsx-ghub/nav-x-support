package com.wiswm.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.DrawableResource

expect class CameraUtils() {
    @Composable
    fun CameraPreview(
        modifier: Modifier,
        iconBack: DrawableResource,
        iconCapture: DrawableResource,
        onBackClick: () -> Unit,
        onCaptureClick: () -> Unit
    )

    @Composable
    fun PhotoPreviewContent(
        modifier: Modifier,
        photoPath: String?,
        iconBack: DrawableResource,
        iconPhoto: DrawableResource,
        iconConfirm: DrawableResource,
        onBackClick: () -> Unit,
        onRetakeClick: () -> Unit,
        onConfirmClick: () -> Unit
    )

    fun capturePhoto(onPhotoCaptured: (String?) -> Unit)

    fun deleteFile(filePath: String?): Boolean

    fun deleteFolder(): Boolean

    fun getCameraPermissionStatus(): CameraPermissionStatus

    fun requestCameraPermission()
}

enum class CameraPermissionStatus {
    GRANTED,
    DENIED,
    NOT_DETERMINED
}