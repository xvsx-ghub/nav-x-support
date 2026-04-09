package com.wiswm.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiswm.nav.camera.resources.Colors
import org.jetbrains.compose.resources.DrawableResource

class CameraScreen(
    val taskTag: String,
    val onClose: (taskTag: String, photoPath: String?) -> Unit
) {
    companion object {
        const val TAG = "CameraScreen"
    }

    @Composable
    fun Content(
        iconBack: DrawableResource,
        iconCapture: DrawableResource,
        iconPhoto: DrawableResource,
        iconConfirm: DrawableResource
    ) {
        val cameraViewModel = remember { CameraViewModel(taskTag, onClose) }

        Permission {
            Box(modifier = Modifier.background(Color.Black)) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = Colors.PacificBlue,
                    content = { padding ->
                        ContentView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            cameraViewModel = cameraViewModel,
                            iconBack = iconBack,
                            iconCapture = iconCapture,
                            iconPhoto = iconPhoto,
                            iconConfirm = iconConfirm
                        )
                    }
                )
            }
        }
    }

    @Composable
    private fun ContentView(
        modifier: Modifier,
        cameraViewModel: CameraViewModel,
        iconBack: DrawableResource,
        iconCapture: DrawableResource,
        iconPhoto: DrawableResource,
        iconConfirm: DrawableResource
    ) {
        if (cameraViewModel.state.showPhotoPreviewStatus && cameraViewModel.state.photoPath != null) {
            PhotoPreview(
                modifier = modifier,
                cameraViewModel = cameraViewModel,
                iconBack = iconBack,
                iconPhoto = iconPhoto,
                iconConfirm = iconConfirm
            )
        } else {
            CameraPreview(
                modifier = modifier,
                cameraViewModel = cameraViewModel,
                iconBack = iconBack,
                iconCapture = iconCapture
            )
        }
    }

    @Composable
    fun CameraPreview(
        modifier: Modifier,
        cameraViewModel: CameraViewModel,
        iconBack: DrawableResource,
        iconCapture: DrawableResource
    ) {
        cameraViewModel.state.permissionStatus?.let {
            when (it) {
                CameraPermissionStatus.GRANTED -> {
                    CameraView(
                        modifier = modifier,
                        cameraViewModel = cameraViewModel,
                        iconBack = iconBack,
                        iconCapture = iconCapture
                    )
                }

                CameraPermissionStatus.DENIED -> {
                    PermissionDeniedView(modifier = modifier, cameraViewModel = cameraViewModel)
                }

                CameraPermissionStatus.NOT_DETERMINED -> {
                    PermissionRequestView(modifier = modifier, cameraViewModel = cameraViewModel)
                }
            }
        }
    }

    @Composable
    fun PhotoPreview(
        modifier: Modifier,
        cameraViewModel: CameraViewModel,
        iconBack: DrawableResource,
        iconPhoto: DrawableResource,
        iconConfirm: DrawableResource
    ) {
        cameraViewModel.getPhotoPreview(
            modifier = modifier,
            iconBack = iconBack,
            iconPhoto = iconPhoto,
            iconConfirm = iconConfirm
        )
    }

    @Composable
    fun CameraView(
        modifier: Modifier,
        cameraViewModel: CameraViewModel,
        iconBack: DrawableResource,
        iconCapture: DrawableResource
    ) {
        cameraViewModel.getCameraPreview(
            modifier = modifier.fillMaxSize(),
            iconBack = iconBack,
            iconCapture = iconCapture
        )
    }

    @Composable
    private fun PermissionRequestView(modifier: Modifier, cameraViewModel: CameraViewModel) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Camera Permission",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "This app needs camera permission to take photos. Please grant permission to continue.",
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { cameraViewModel.requestPermission() },
                        colors = ButtonDefaults.buttonColors(containerColor = Colors.PacificBlue)
                    ) {
                        Text(
                            text = "Grant Permission",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun PermissionDeniedView(modifier: Modifier, cameraViewModel: CameraViewModel) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Camera Permission Denied",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Camera permission was denied. Please enable it in settings to take photos.",
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { cameraViewModel.requestPermission() },
                        colors = ButtonDefaults.buttonColors(containerColor = Colors.PacificBlue)
                    ) {
                        Text(
                            text = "Try Again",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}