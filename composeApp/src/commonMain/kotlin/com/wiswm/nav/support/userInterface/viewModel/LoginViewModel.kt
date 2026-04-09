package com.wiswm.nav.support.userInterface.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.useCase.LoginUseCase
import com.wiswm.nav.support.util.Logger
import com.wiswm.nav.support.util.System
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    val system: System,
    val loginUseCase: LoginUseCase,
    val repositoryLocal: RepositoryLocal
) : ViewModel() {
    companion object {
        const val TAG = "LoginViewModel"
    }

    data class State(
        val uiNotificationMessage: String? = null,
        val progressBarVisibilityStatus: Boolean = false,
        val qrCodeScannerVisibilityStatus: Boolean = false
    )

    var state by mutableStateOf(State())
        private set

    enum class Event {
        UNKNOWN,
        START_DASHBOARD_SCREEN
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

    fun setQrCodeScannerState(visible: Boolean) {
        state = state.copy(qrCodeScannerVisibilityStatus = visible)
    }

    fun setProgressBarState(visible: Boolean) {
        state = state.copy(progressBarVisibilityStatus = visible)
    }

    fun setUiNotification(message: String?) {
        viewModelScope.launch {
            state = state.copy(uiNotificationMessage = message)
            delay(3000)
            state = state.copy(uiNotificationMessage = null)
        }
    }

    fun getAppVersion(): String {
        return system.getAppVersion()
    }

    fun startDashboardScreen() {
        emitEvent(Event.START_DASHBOARD_SCREEN)
    }

    fun login(data: String) {
        Logger.d(TAG, "login(): $data")

        if (!isWisData(data)) return

        viewModelScope.launch {
            loginUseCase.invoke(
                data = data,
                hardwareModel = system.getDeviceInfo().model,
                osModel = system.getDeviceInfo().osVersion,
                applicationModel = system.getAppVersion(),
                onEvent = { event, errorMessage ->
                    when (event) {
                        LoginUseCase.Event.STARTED -> {
                            setProgressBarState(true)
                        }

                        LoginUseCase.Event.COMPLETED -> {
                            setProgressBarState(false)
                            if (errorMessage == null) {
                                repositoryLocal.clearTaskEntity()
                                startDashboardScreen()
                            } else {
                                Logger.d(TAG, "login(): Error: $errorMessage")
                                setUiNotification(errorMessage)
                            }
                        }

                        else -> {}
                    }
                    Logger.d(TAG, "login(): " + event.name)
                })
        }
    }

    private fun isWisData(result: String) = result.startsWith("WIS:;:")

    fun isAuthorizationRequired(): Boolean{
        return repositoryLocal.getSessionKey().isEmpty()
    }
}