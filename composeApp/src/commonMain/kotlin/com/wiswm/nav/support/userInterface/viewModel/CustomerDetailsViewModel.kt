package com.wiswm.nav.support.userInterface.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.dataBase.entity.CustomerEntity
import com.wiswm.nav.support.data.local.dataBase.entity.CustomerTaskEntity
import com.wiswm.nav.support.data.local.dataBase.entity.NotServicingReasonEntity
import com.wiswm.nav.support.data.local.dataBase.entity.TaskEntity
import com.wiswm.nav.support.data.remote.http.Http
import com.wiswm.nav.support.data.remote.http.HttpClientCore
import com.wiswm.nav.support.data.remote.stomp.Stomp
import com.wiswm.nav.support.data.remote.stomp.StompManager
import com.wiswm.nav.support.resources.Strings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.Boolean
import kotlin.toString

class CustomerDetailsViewModel(
    val stompManager: StompManager,
    val repositoryLocal: RepositoryLocal,
    val http: Http
) : ViewModel() {
    companion object {
        const val TAG = "CustomerDetailsViewModel"
    }

    data class State(
        val uiNotificationMessage: String?,
        val stompConnectionEvent: Stomp.Event,
        val customerEntity: CustomerEntity?,
        val customerTaskEntityList: List<CustomerTaskEntity>?,
        val updateUiTrigger: Boolean,
        val notServicingReasonDialogVisibilityStatus: Boolean,
        val notServicingReasonEntityList: List<NotServicingReasonEntity>?,
        val selectedCustomerEntity: CustomerEntity?,
        val createdTaskEntity: TaskEntity?,
        val progressBarVisibilityStatus: Boolean
    )

    var customerTaskEntityJob: Job? = null

    var state by mutableStateOf(
        State(
            uiNotificationMessage = null,
            stompConnectionEvent = stompManager.getLastStompEvent() ?: Stomp.Event.UNKNOWN,
            customerEntity = null,
            customerTaskEntityList = null,
            updateUiTrigger = false,
            notServicingReasonDialogVisibilityStatus = false,
            notServicingReasonEntityList = null,
            selectedCustomerEntity = null,
            createdTaskEntity = null,
            progressBarVisibilityStatus = false
        )
    )
        private set

    enum class Event {
        UNKNOWN,
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

    fun setProgressBarState(visible: Boolean) {
        state = state.copy(progressBarVisibilityStatus = visible)
    }

    init {
        serveStomp()
        serveCustomerTaskList()
        serveNotServicingReasonList()
    }

    fun serveStomp() {
        viewModelScope.launch {
            stompManager.collectStompEvent { event ->
                state = state.copy(stompConnectionEvent = event)
            }
        }
    }

    private fun serveCustomerTaskList() {
        customerTaskEntityJob?.cancel()

        viewModelScope.launch {
            state.customerEntity?.customerRefId?.let { customerRefId ->
                repositoryLocal.getCustomerTaskEntityByCustomerRefIdList(customerRefId)?.let {
                    state = state.copy(
                        customerTaskEntityList = it,
                        updateUiTrigger = !state.updateUiTrigger
                    )
                }
            }
        }

        customerTaskEntityJob = viewModelScope.launch {
            state.customerEntity?.customerRefId?.let { customerRefId ->
                repositoryLocal.getCustomerTaskEntityByCustomerRefIdListAsFlow(
                    customerRefId
                ).collect {
                    state = state.copy(
                        customerTaskEntityList = it,
                        updateUiTrigger = !state.updateUiTrigger
                    )
                }
            }
        }
    }

    private fun serveNotServicingReasonList() {
        viewModelScope.launch {
            repositoryLocal.getNotServicingReasonEntityList()?.let {
                state = state.copy(notServicingReasonEntityList = it)
            }
        }

        viewModelScope.launch {
            repositoryLocal.getNotServicingReasonEntityListAsFlow().collect {
                state = state.copy(notServicingReasonEntityList = it)
            }
        }
    }

    fun serveCameraScreen(taskTag: String, photoPath: String) {
        viewModelScope.launch {
            state.createdTaskEntity?.let {
                updateTask(it, photoPath)
            }
        }
    }

    fun getPhoto() {
        emitEvent(Event.GET_PHOTO)
    }

    fun setCustomerEntity(customerEntity: CustomerEntity) {
        state = state.copy(customerEntity = customerEntity)
        serveCustomerTaskList()
    }

    fun pushNotServicingReasonDialog(show: Boolean) {
        state = state.copy(notServicingReasonDialogVisibilityStatus = show)
    }

    fun createNotServicingReasonPhotoTask(notServicingReasonEntity: NotServicingReasonEntity) {
        viewModelScope.launch {
            state.selectedCustomerEntity?.let { nnSelectedCustomerEntity ->
                val userId = repositoryLocal.getAuthorizationEntity()?.userId
                val truckId = repositoryLocal.getAuthorizationEntity()?.truckId
                val crewId = "$userId$truckId"
                val taskEntity = TaskEntity(
                    type = TaskEntity.TAKE_PHOTO_TYPE,
                    customerId = nnSelectedCustomerEntity.customerId.toString(),
                    customerRefId = nnSelectedCustomerEntity.customerRefId,
                    liftId = repositoryLocal.getLocalGeneratedTransactionId().toString(),
                    deviceId = crewId,
                    timestamp = repositoryLocal.getFormattedCurrentTimeSeconds(),
                    reasonId = notServicingReasonEntity.key,
                    status = TaskEntity.NEW_STATUS,
                    filePath = ""
                )
                val id = repositoryLocal.insert(taskEntity)
                val dbTaskEntity = repositoryLocal.getTaskEntity(id)
                state = state.copy(createdTaskEntity = dbTaskEntity)
                getPhoto()
            }
        }
    }

    fun setSelectedCustomerEntity(customerId: String, onComplete: (status: Boolean) -> Unit) {
        viewModelScope.launch {
            val id = customerId.toIntOrNull()
            id?.let { customerId ->
                val customerEntity = repositoryLocal.getByCustomerId(customerId)
                customerEntity?.let { nnCustomerEntity ->
                    state = state.copy(selectedCustomerEntity = nnCustomerEntity)
                    onComplete(true)
                    return@launch
                }
            }
            onComplete(false)
        }
    }

    private fun updateTask(taskEntity: TaskEntity, filePath: String) {
        viewModelScope.launch {
            taskEntity.filePath = filePath
            val taskEntity = taskEntity
            repositoryLocal.update(taskEntity)
            when (taskEntity.type) {

                TaskEntity.TAKE_PHOTO_TYPE -> {
                    http.setPhoto(
                        customerId = taskEntity.customerId,
                        transactionId = taskEntity.liftId,
                        receivedDeviceId = taskEntity.deviceId,
                        timeStamp = taskEntity.timestamp,
                        notServicingReasonId = taskEntity.reasonId,
                        imagePath = taskEntity.filePath,
                        taskIds = getTasksIdsString(taskEntity.customerId) ?: "",
                        onEvent = { status, data, error ->
                            when (status) {
                                HttpClientCore.HttpStatus.Started -> {
                                    setProgressBarState(true)
                                }

                                HttpClientCore.HttpStatus.Completed -> {
                                    if(error != null){
                                        setUiNotification(Strings.PHOTO_NOT_SENT + "/n" + error)
                                        taskEntity.status = TaskEntity.PENDING_STATUS
                                        repositoryLocal.update(taskEntity)
                                    }else{
                                        setUiNotification(Strings.PHOTO_SENT_SUCCESSFUL)
                                        taskEntity.status = TaskEntity.DONE_STATUS
                                        repositoryLocal.update(taskEntity)
                                    }
                                    setProgressBarState(false)
                                }

                                else -> {}
                            }
                        }
                    )
                }

                TaskEntity.TAKE_SIGNATURE_TYPE -> {
                    http.setSignature(
                        customerId = taskEntity.customerId,
                        transactionId = taskEntity.liftId,
                        receivedDeviceId = taskEntity.deviceId,
                        notServicingReasonId = taskEntity.reasonId,
                        imagePath = taskEntity.filePath,
                        taskIds = getTasksIdsString(taskEntity.customerId) ?: "",
                        onEvent = { status, data, error ->
                            when (status) {
                                HttpClientCore.HttpStatus.Started -> {
                                    setProgressBarState(true)
                                }

                                HttpClientCore.HttpStatus.Completed -> {
                                    if(error != null){
                                        setUiNotification(Strings.SIGNATURE_NOT_SENT + "/n" + error)
                                        taskEntity.status = TaskEntity.PENDING_STATUS
                                        repositoryLocal.update(taskEntity)
                                    }else{
                                        setUiNotification(Strings.SIGNATURE_SENT_SUCCESSFUL)
                                        taskEntity.status = TaskEntity.DONE_STATUS
                                        repositoryLocal.update(taskEntity)
                                    }
                                    setProgressBarState(false)
                                }

                                else -> {}
                            }
                        }
                    )
                }

                else -> {}
            }
        }
    }

    private suspend fun getTasksIdsString(customerId: String?) =
        customerId?.let { nnCustomerId ->
            repositoryLocal.getCustomerTaskEntityByCustomerIdList(nnCustomerId)
                ?.joinToString(separator = ",") {
                    it.taskId
                }
        }
}