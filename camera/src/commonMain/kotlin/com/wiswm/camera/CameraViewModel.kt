package com.wiswm.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import org.jetbrains.compose.resources.DrawableResource

class CameraViewModel(
    val taskTag: String,
    val onClose: (taskTag: String, photoPath: String?) -> Unit
) : ViewModel() {
    companion object {
        const val TAG = "CameraViewModel"
    }

    data class State(
        val taskTag: String,
        val photoPath: String?,
        val permissionStatus: CameraPermissionStatus?,
        val showPhotoPreviewStatus: Boolean
    )

    var state by mutableStateOf(
        State(
            taskTag = taskTag,
            photoPath = null,
            permissionStatus = null,
            showPhotoPreviewStatus = false
        )
    )
        private set

    var cameraUtils = CameraUtils()

    init {
        state = state.copy(permissionStatus = cameraUtils.getCameraPermissionStatus())
    }

    @Composable
    fun getCameraPreview(
        modifier: Modifier,
        iconBack: DrawableResource,
        iconCapture: DrawableResource
    ): Any {
        return cameraUtils.CameraPreview(
            modifier,
            iconBack = iconBack,
            iconCapture = iconCapture,
            onBackClick = {
                deletePhoto()
                onClose(state.taskTag, state.photoPath)
            },
            onCaptureClick = {
                cameraUtils.capturePhoto { photoPath ->
                    state = state.copy(photoPath = photoPath)
                    previewPhoto()
                }
            }
        )
    }

    @Composable
    fun getPhotoPreview(
        modifier: Modifier,
        iconBack: DrawableResource,
        iconPhoto: DrawableResource,
        iconConfirm: DrawableResource
    ): Any {
        return cameraUtils.PhotoPreviewContent(
            modifier = modifier,
            photoPath = state.photoPath,
            iconBack = iconBack,
            iconPhoto = iconPhoto,
            iconConfirm = iconConfirm,
            onBackClick = {
                deletePhoto()
                onClose(state.taskTag, state.photoPath)
            },
            onRetakeClick = {
                deletePhoto()
                previewCamera()
            },
            onConfirmClick = {
                confirmPhoto()
                onClose(state.taskTag, state.photoPath)
            }
        )
    }

    fun requestPermission() {
        cameraUtils.requestCameraPermission()
        state = state.copy(permissionStatus = cameraUtils.getCameraPermissionStatus())
    }

    fun deletePhoto() {
        state.photoPath?.let {
            cameraUtils.deleteFile(it)
            state = state.copy(photoPath = null)
        }
    }

    fun confirmPhoto() {
        state.photoPath?.let {
            state = state.copy(showPhotoPreviewStatus = false)
        }
    }

    fun previewPhoto() {
        state = state.copy(showPhotoPreviewStatus = true)
    }

    fun previewCamera() {
        state = state.copy(showPhotoPreviewStatus = false)
    }
}