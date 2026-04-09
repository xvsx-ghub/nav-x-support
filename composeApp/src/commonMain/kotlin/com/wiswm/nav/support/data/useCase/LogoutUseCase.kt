package com.wiswm.nav.support.data.useCase

/*
import com.wiswm.nav.camera.CameraUtils
import com.wiswm.nav.drawingpad.DrawingPadUtils
*/
import com.wiswm.nav.support.data.local.RepositoryLocal

class LogoutUseCase(
    private val repositoryLocal: RepositoryLocal,
    //private val cameraUtils: CameraUtils,
    //private val drawingPadUtils: DrawingPadUtils
) {
    enum class Event {
        Unknown,
        Started,
        Completed
    }

    suspend operator fun invoke(
        onEvent: suspend (event: Event, errorMessage: String?) -> Unit
    ) {
        onEvent(Event.Started, null)
        repositoryLocal.clearRepository()
        /*
        cameraUtils.deleteFolder()
        drawingPadUtils.deleteFolder()
        */
        onEvent(Event.Completed, null)
    }
}