package com.wiswm.qr_code_scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
expect fun Scanner(
    modifier: Modifier = Modifier,
    onScanned: (String) -> Boolean,
    types: List<CodeType>,
    cameraPosition: CameraPosition = CameraPosition.BACK
)

@Composable
fun ScannerWithPermissions(
    modifier: Modifier = Modifier,
    onScanned: (String) -> Boolean,
    types: List<CodeType>,
    cameraPosition: CameraPosition,
    permissionDeniedContent: @Composable (CameraPermissionState) -> Unit
) {
    val permissionState = rememberCameraPermissionState()

    LaunchedEffect(Unit) {
        if (permissionState.status == CameraPermissionStatus.Denied) {
            permissionState.requestCameraPermission()
        }
    }

    if (permissionState.status == CameraPermissionStatus.Granted) {
        Scanner(modifier, types = types, onScanned = onScanned, cameraPosition = cameraPosition)
    } else {
        permissionDeniedContent(permissionState)
    }
}