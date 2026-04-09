package com.wiswm.camera

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import platform.AVFoundation.*
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject

actual class CameraUtils actual constructor() {
    private var captureSession: AVCaptureSession? = null
    private var previewLayer: AVCaptureVideoPreviewLayer? = null
    private var previewView: UIView? = null
    private var photoOutput: AVCapturePhotoOutput? = null

    @Composable
    actual fun CameraPreview(
        modifier: Modifier,
        iconBack: DrawableResource,
        iconCapture: DrawableResource,
        onBackClick: () -> Unit,
        onCaptureClick: () -> Unit
    ) {
        DisposableEffect(Unit) {
            onDispose {
                stopSession()
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {

            UIKitView(
                factory = { createCameraPreviewView() },
                modifier = modifier.fillMaxSize(),
                onRelease = { stopSession() }
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

    @OptIn(ExperimentalForeignApi::class)
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
                val fileManager = NSFileManager.defaultManager
                if (fileManager.fileExistsAtPath(path)) {
                    UIKitView(
                        factory = {
                            val imageView = UIImageView()
                            val image = UIImage.imageWithContentsOfFile(path)
                            image?.let { uiImage ->
                                imageView.image = uiImage
                                imageView.contentMode =
                                    UIViewContentMode.UIViewContentModeScaleAspectFill
                                imageView.clipsToBounds = true
                                imageView.layer.cornerRadius = 12.0
                            }
                            imageView
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "File not found",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
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
                        text = "No image to display",
                        color = Color.White,
                        fontSize = 16.sp
                    )
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

    @OptIn(ExperimentalForeignApi::class)
    actual fun capturePhoto(onPhotoCaptured: (String?) -> Unit) {
        val session = captureSession ?: return onPhotoCaptured(null)
        val output = photoOutput ?: AVCapturePhotoOutput().also {
            if (session.canAddOutput(it)) session.addOutput(it)
            photoOutput = it
        }

        val settings = AVCapturePhotoSettings.photoSettingsWithFormat(
            mapOf(AVVideoCodecKey to AVVideoCodecTypeJPEG)
        )

        val delegate = object : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
            override fun captureOutput(
                output: AVCapturePhotoOutput,
                didFinishProcessingPhoto: AVCapturePhoto,
                error: NSError?
            ) {
                if (error != null) {
                    println("📸 Error capturing photo: ${error.localizedDescription}")
                    onPhotoCaptured(null)
                    return
                }

                val imageData = didFinishProcessingPhoto.fileDataRepresentation()
                if (imageData != null) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val path = withContext(Dispatchers.Default) {
                            saveToFile(imageData)
                        }
                        onPhotoCaptured(path)
                    }
                } else {
                    onPhotoCaptured(null)
                }
            }
        }
        output.capturePhotoWithSettings(settings, delegate)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun deleteFile(filePath: String?): Boolean {
        filePath?.let{
            val fileManager = NSFileManager.defaultManager
            val errorPtr = nativeHeap.alloc<ObjCObjectVar<NSError?>>()

            val success = fileManager.removeItemAtPath(it, errorPtr.ptr)
            if (!success) {
                val error = errorPtr.value
                println("Failed to delete file: ${error?.localizedDescription}")
            }

            nativeHeap.free(errorPtr)
            return success
        }
        return false
    }

    actual fun getCameraPermissionStatus(): CameraPermissionStatus {
        return when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> CameraPermissionStatus.GRANTED
            AVAuthorizationStatusDenied -> CameraPermissionStatus.DENIED
            AVAuthorizationStatusNotDetermined -> CameraPermissionStatus.NOT_DETERMINED
            else -> CameraPermissionStatus.NOT_DETERMINED
        }
    }

    actual fun requestCameraPermission() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            println("📷 Camera permission granted: $granted")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun createCameraPreviewView(): UIView {
        if (previewView != null) return previewView!!

        val view = UIView(frame = UIScreen.mainScreen.bounds)
        val session = AVCaptureSession()
        session.sessionPreset = AVCaptureSessionPresetPhoto

        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (device != null) {
            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null)
            input?.let {
                if (session.canAddInput(it)) session.addInput(it)
                val layer = AVCaptureVideoPreviewLayer(session = session)
                layer.videoGravity = AVLayerVideoGravityResizeAspectFill
                layer.frame = view.bounds
                view.layer.addSublayer(layer)

                session.startRunning()
                captureSession = session
                previewLayer = layer
                previewView = view
            }
        }

        return view
    }

    private fun stopSession() {
        captureSession?.let {
            if (it.isRunning()) {
                it.stopRunning()
                println("📷 Camera session stopped")
            }
        }
        captureSession = null
        previewLayer = null
        previewView = null
        photoOutput = null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun saveToFile(imageData: NSData): String? {
        val filePath = createFilePath() ?: return null
        val success = imageData.writeToFile(filePath, true)
        return if (success) filePath else null
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun createFilePath(): String? {
        val dir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() as? String ?: return null

        val photosDir = "$dir/photos"
        val fileManager = NSFileManager.defaultManager

        memScoped {
            val isDir = alloc<BooleanVar>()
            val exists = fileManager.fileExistsAtPath(photosDir, isDir.ptr)
            if (!exists || !isDir.value) {
                fileManager.createDirectoryAtPath(
                    path = photosDir,
                    withIntermediateDirectories = true,
                    attributes = null,
                    error = null
                )
            }
        }

        val formatter = NSDateFormatter().apply {
            dateFormat = "yyyyMMdd_HHmmss"
        }
        val name = formatter.stringFromDate(NSDate())
        return "$photosDir/photo_$name.jpg"
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun deleteFolder(): Boolean {
        val dir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: return false

        val photosDir = "$dir/photos"
        val fileManager = NSFileManager.defaultManager

        memScoped {
            val isDir = alloc<BooleanVar>()
            val exists = fileManager.fileExistsAtPath(photosDir, isDir.ptr)

            if (!exists || !isDir.value) return false
        }

        val errorPtr = memScoped { alloc<ObjCObjectVar<NSError?>>() }

        val success = fileManager.removeItemAtPath(
            path = photosDir,
            error = errorPtr.ptr
        )

        if (!success) {
            val err = errorPtr.value
            println("Failed to delete folder: ${err?.localizedDescription}")
        }

        return success
    }
}