package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.wiswm.camera.CameraScreen
import com.wiswm.nav.support.resources.Colors
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_back
import navxsupportapp.composeapp.generated.resources.ic_capture
import navxsupportapp.composeapp.generated.resources.ic_confirm
import navxsupportapp.composeapp.generated.resources.ic_photo

class CameraModuleScreen(val taskTag: String, val onClose: (taskTag: String, photoPath: String?)-> Unit) : Screen {
    companion object Companion {
        const val TAG = "CameraModuleScreen"
    }

    @Composable
    override fun Content() {
        Box(modifier = Modifier.background(Colors.Black)) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                content = {
                    CameraScreen(
                        taskTag = taskTag,
                        onClose = onClose
                    ).Content(
                        iconBack = Res.drawable.ic_back,
                        iconCapture = Res.drawable.ic_capture,
                        iconPhoto = Res.drawable.ic_photo,
                        iconConfirm = Res.drawable.ic_confirm
                    )
                }
            )
        }
    }
}