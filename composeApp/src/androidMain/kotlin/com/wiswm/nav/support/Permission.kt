package com.wiswm.nav.support

import android.Manifest
import android.os.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun Permission(
    content: @Composable () -> Unit
) {
    val permissions: MutableList<String> = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.VIBRATE,
        Manifest.permission.INTERNET
    )
    val rationale: String = stringResource(R.string.default_permission_rationale)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }
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
            Text(text = stringResource(R.string.permission_request))
        },
        text = {
            Text(text)
        },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text(stringResource(R.string.ok))
            }
        }
    )
}