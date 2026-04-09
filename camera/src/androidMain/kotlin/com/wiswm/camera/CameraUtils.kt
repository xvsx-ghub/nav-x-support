package com.wiswm.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import java.io.File
import java.io.FileOutputStream

lateinit var cameraContext: Context

actual class CameraUtils actual constructor() {
    companion object {
        private const val TAG = "CameraUtils"
        private var boundImageCapture: ImageCapture? = null
    }

    @Composable
    actual fun CameraPreview(
        modifier: Modifier,
        iconBack: DrawableResource,
        iconCapture: DrawableResource,
        onBackClick: () -> Unit,
        onCaptureClick: () -> Unit
    ) {
        val context = LocalContext.current
        cameraContext = context
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

        var hasPermission by remember { mutableStateOf(false) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted -> hasPermission = granted }
        )

        LaunchedEffect(Unit) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) launcher.launch(Manifest.permission.CAMERA)
            else hasPermission = true
        }

        if (hasPermission) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {

                AndroidView(
                    modifier = modifier,
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val preview = Preview.Builder().build()
                        val selector = CameraSelector.DEFAULT_BACK_CAMERA

                        val imageCapture = ImageCapture.Builder().build()

                        previewView.post {
                            previewView.display?.let {
                                imageCapture.targetRotation = it.rotation
                            }
                        }

                        cameraProviderFuture.addListener({
                            try {
                                val provider = cameraProviderFuture.get()
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    imageCapture
                                )
                                preview.setSurfaceProvider(previewView.surfaceProvider)
                                boundImageCapture = imageCapture
                                Log.d(TAG, "Camera bound successfully")
                            } catch (e: Exception) {
                                Log.e(TAG, "Camera binding failed: ${e.message}")
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                            previewView.display?.let {
                                boundImageCapture?.targetRotation = it.rotation
                            }
                        }

                        previewView
                    }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(vertical = 24.dp, horizontal = 32.dp)
                ) {
                    IconButton(
                        modifier = Modifier.align(Alignment.CenterStart),
                        onClick = onBackClick) {
                        Icon(
                            painter = painterResource(iconBack),
                            contentDescription = "Discard photo",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        modifier = Modifier.align(Alignment.Center),
                        onClick = onCaptureClick) {
                        Icon(
                            painter = painterResource(iconCapture),
                            contentDescription = "Capture photo",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }

    @Composable
    actual fun PhotoPreviewContent(
        modifier: Modifier,
        photoPath: String?,
        iconBack: DrawableResource,
        iconPhoto: DrawableResource,
        iconConfirm: DrawableResource,
        onBackClick: () -> Unit,
        onRetakeClick: () -> Unit,
        onConfirmClick: () -> Unit
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            photoPath?.let { path ->
                val imageBitmap = loadImageFromPath(path)
                imageBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Captured photo preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray),
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Failed to load image",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(vertical = 24.dp, horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(iconBack),
                        contentDescription = "Close screen",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onRetakeClick) {
                    Icon(
                        painter = painterResource(iconPhoto),
                        contentDescription = "Retake photo",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onConfirmClick) {
                    Icon(
                        painter = painterResource(iconConfirm),
                        contentDescription = "Confirm photo",
                        tint = Color.White
                    )
                }
            }
        }
    }

    actual fun capturePhoto(onPhotoCaptured: (String?) -> Unit) {
        val imageCapture = boundImageCapture
        if (imageCapture == null) {
            Log.e(TAG, "ImageCapture not ready")
            onPhotoCaptured(null)
            return
        }

        val path = createFilePath(cameraContext) ?: run {
            Log.e(TAG, "Failed to create file path")
            onPhotoCaptured(null)
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(File(path)).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(cameraContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo captured: $path")
                    fixExifRotation(path)
                    onPhotoCaptured(path)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Capture failed: ${exception.message}")
                    onPhotoCaptured(null)
                }
            }
        )
    }

    actual fun deleteFile(filePath: String?): Boolean {
        if (filePath.isNullOrEmpty()) return false
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete().also { deleted ->
                    if (!deleted) {
                        Log.e(TAG, "Failed to delete file: $filePath")
                    } else {
                        Log.d(TAG, "File deleted: $filePath")
                    }
                }
            } else {
                Log.w(TAG, "File not found: $filePath")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: ${e.message}")
            false
        }
    }

    actual fun getCameraPermissionStatus(): CameraPermissionStatus {
        val granted =
            ContextCompat.checkSelfPermission(cameraContext, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        return if (granted) CameraPermissionStatus.GRANTED else CameraPermissionStatus.DENIED
    }

    actual fun requestCameraPermission() {
        Log.d(TAG, "Camera permission is handled automatically in CameraPreview")
    }

    private fun createFilePath(context: Context): String? {
        return try {
            val dir = File(context.filesDir, "photos")
            if (!dir.exists()) dir.mkdirs()
            val filename = "image_${System().getCurrentTimeMillis()}.jpg"
            File(dir, filename).absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create file path: ${e.message}")
            null
        }
    }

    actual fun deleteFolder(): Boolean {
        return try {
            val dir = File(cameraContext.filesDir, "photos")
            if (dir.exists()) {
                dir.deleteRecursively()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete folder: ${e.message}")
            false
        }
    }

    private fun fixExifRotation(path: String) {
        try {
            val exif = ExifInterface(path)
            val rotation = when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            if (rotation != 0) {
                val bitmap = BitmapFactory.decodeFile(path)
                val matrix = Matrix()
                matrix.postRotate(rotation.toFloat())
                val rotated =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                FileOutputStream(path).use { out ->
                    rotated.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }

                exif.setAttribute(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL.toString()
                )
                exif.saveAttributes()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fix EXIF rotation: ${e.message}")
        }
    }

    private fun loadImageFromPath(path: String): ImageBitmap? {
        return try {
            val file = File(path)
            if (!file.exists()) {
                null
            } else {
                val bitmap = BitmapFactory.decodeFile(path)
                bitmap?.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }
}