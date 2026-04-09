package com.wiswm.nav.support.userInterface.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.dataBase.entity.TaskEntity
import com.wiswm.nav.support.data.remote.stomp.Stomp
import com.wiswm.nav.support.data.remote.stomp.StompManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class TaskListViewModel(val stompManager: StompManager, val repositoryLocal: RepositoryLocal) : ViewModel() {
    companion object {
        const val TAG = "DashboardViewModel"
    }

    data class State(
        val uiNotificationMessage: String?,
        val stompConnectionEvent: Stomp.Event,
        val taskEntityList: List<TaskEntity>?,
        val selectedTaskEntity: TaskEntity?,
        val updateUiTrigger: Boolean
    )

    var state by mutableStateOf(
        State(
            uiNotificationMessage = null,
            stompConnectionEvent = stompManager.getLastStompEvent() ?: Stomp.Event.UNKNOWN,
            taskEntityList = null,
            selectedTaskEntity = null,
            updateUiTrigger = false
        ))
        private set

    enum class Event {
        UNKNOWN,
        GET_SIGNATURE,
        GET_PHOTO
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

    fun setUiNotification(message: String?) {
        state = state.copy(uiNotificationMessage = message)
        viewModelScope.launch {
            delay(3000)
            state = state.copy(uiNotificationMessage = null)
        }
    }

    init {
        serveStomp()
        serveTaskList()
    }

    fun serveStomp(){
        viewModelScope.launch {
            stompManager.collectStompEvent { event ->
                state = state.copy(stompConnectionEvent = event)
            }
        }
    }

    fun serveTaskList(){
        viewModelScope.launch {
            val taskEntityList = repositoryLocal.getTaskEntityList()
            state = state.copy(taskEntityList = taskEntityList, updateUiTrigger = !state.updateUiTrigger)
        }

        viewModelScope.launch {
            repositoryLocal.getTaskEntityListAsFlow().collect { taskEntityList ->
                state = state.copy(taskEntityList = taskEntityList, updateUiTrigger = !state.updateUiTrigger)
            }
        }
    }

    fun setSelectedTaskEntity(taskEntity: TaskEntity) {
        state = state.copy(selectedTaskEntity = taskEntity)
    }

    fun getSignature() {
        emitEvent(Event.GET_SIGNATURE)
    }

    fun getPhoto() {
        emitEvent(Event.GET_PHOTO)
    }
}