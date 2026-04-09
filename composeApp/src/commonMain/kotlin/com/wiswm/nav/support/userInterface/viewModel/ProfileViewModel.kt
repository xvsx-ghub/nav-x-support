package com.wiswm.nav.support.userInterface.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.wiswm.nav.camera.CameraUtils
//import com.wiswm.nav.drawingpad.DrawingPadUtils
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.dataBase.entity.AuthorizationEntity
import com.wiswm.nav.support.data.useCase.LogoutUseCase
import com.wiswm.nav.support.util.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repositoryLocal: RepositoryLocal
) : ViewModel() {
    companion object {
        const val TAG = "ProfileViewModel"
    }

    enum class Event {
        UNKNOWN,
        LOG_OUT,
    }

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()
    private fun emitEvent(event: Event) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    suspend fun collectEvent(onEvent: (event: Event) -> Unit) {
        eventFlow.collect { event ->
            onEvent(event)
        }
    }

    data class State(
        val uiNotificationMessage: String?,
        val progressBarVisibilityStatus: Boolean,
        val authorizationEntity: AuthorizationEntity?,
        val wisName: String?
    )

    var state by mutableStateOf(
        State(
            uiNotificationMessage = null,
            progressBarVisibilityStatus = false,
            authorizationEntity = null,
            wisName = null
        )
    )
        private set

    init {
        serveProfileData()
    }

    private fun pushProgressBar(visible: Boolean) {
        state = state.copy(progressBarVisibilityStatus = visible)
    }

    private fun pushUiNotification(message: String?) {
        state = state.copy(uiNotificationMessage = message)
        viewModelScope.launch {
            delay(1000)
            state = state.copy(uiNotificationMessage = null)
        }
    }

    private fun serveProfileData() {
        viewModelScope.launch {
            state = state.copy(
                authorizationEntity = repositoryLocal.getAuthorizationEntity(),
                wisName = repositoryLocal.getWisName()
            )
        }

        viewModelScope.launch {
            repositoryLocal.getAuthorizationEntityAsFlow().collect {
                Logger.d(TAG, "repositoryLocal.getTruckReportEntityListAsFlow()")
                state = state.copy(
                    authorizationEntity = it,
                    wisName = repositoryLocal.getWisName()
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            LogoutUseCase(
                repositoryLocal = repositoryLocal,
                /*
                cameraUtils = CameraUtils(),
                drawingPadUtils = DrawingPadUtils()
                */
            ).invoke { event, errorMessage ->
                when(event){
                    LogoutUseCase.Event.Started -> {
                        pushProgressBar(true)
                    }
                    LogoutUseCase.Event.Completed -> {
                        if(errorMessage.isNullOrEmpty()){
                            Logger.d(TAG, "emitEvent(Event.LOG_OUT)")
                            emitEvent(Event.LOG_OUT)
                        }else{
                            pushUiNotification(errorMessage)
                        }
                        pushProgressBar(false)
                    }
                    else -> {}
                }
            }
        }
    }
}