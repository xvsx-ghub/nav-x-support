package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.wiswm.drawing_pad.DrawingPadScreen
import com.wiswm.nav.support.resources.Colors
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_back
import navxsupportapp.composeapp.generated.resources.ic_confirm
import navxsupportapp.composeapp.generated.resources.ic_erase

class DrawingPadModuleScreen(
    val taskTag: String,
    val onClose: (taskTag: String, photoPath: String?) -> Unit
) : Screen {
    companion object Companion {
        const val TAG = "DrawingPadModuleScreen"
    }

    @Composable
    override fun Content() {
        Box(modifier = Modifier.background(Colors.Black)) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                content = {
                    DrawingPadScreen(taskTag = taskTag, onClose = onClose).Content(
                        iconBack = Res.drawable.ic_back,
                        iconConfirm = Res.drawable.ic_confirm,
                        iconErase = Res.drawable.ic_erase
                    )
                }
            )
        }
    }
}