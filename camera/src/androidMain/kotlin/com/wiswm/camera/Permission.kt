package com.wiswm.camera

import android.Manifest
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.wiswm.nav.camera.resources.Strings
import kotlin.collections.all

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun Permission(
    content: @Composable () -> Unit
) {
    val permissions: MutableList<String> = mutableListOf(
        Manifest.permission.CAMERA
    )
    val rationale: String = Strings.THIS_PERMISSION_IS_IMPORTANT_

    val permissionsState = rememberMultiplePermissionsState(permissions)
    val allGranted = permissionsState.permissions.all { it.status.isGranted }

    when {
        allGranted -> {
            content()
        }

        else -> {
            Rationale(
                text = rationale,
                onRequestPermission = { permissionsState.launchMultiplePermissionRequest() }
            )
        }
    }
}

@Composable
private fun Rationale(
    text: String,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Don't allow dismiss without action */ },
        title = {
            Text(text = Strings.PERMISSION_REQUEST)
        },
        text = {
            Text(text)
        },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text(Strings.OK)
            }
        }
    )
}