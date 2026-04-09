package com.wiswm.nav.support.userInterface.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.dataBase.entity.AuthorizationEntity
import com.wiswm.nav.support.data.local.dataBase.entity.StompEntity
import com.wiswm.nav.support.data.local.dataBase.entity.TaskEntity
import com.wiswm.nav.support.data.remote.http.Http
import com.wiswm.nav.support.data.remote.http.HttpClientCore.HttpStatus
import com.wiswm.nav.support.data.remote.stomp.subscriber.RouteSubscriber
import com.wiswm.nav.support.data.remote.stomp.StompManager
import com.wiswm.nav.support.data.remote.stomp.StompMessage
import com.wiswm.nav.support.data.remote.stomp.subscriber.TaskSubscriber
import com.wiswm.nav.support.resources.Strings
import com.wiswm.nav.support.util.ConnectivityObserver
import com.wiswm.nav.support.util.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val http: Http,
    private val stompManager: StompManager,
    private val repositoryLocal: RepositoryLocal,
    private val taskListViewModel: TaskListViewModel,
    private val customerListViewModel: CustomerListViewModel,
    private val weighingListViewModel: WeighingListViewModel,
    private val reportViewModel: ReportViewModel,
    private val profileViewModel: ProfileViewModel,
    private val customerDetailsViewModel: CustomerDetailsViewModel,
    private val weighingDetailsViewModel: WeighingDetailsViewModel
) : ViewModel() {
    companion object {
        private const val TAG = "DashboardViewModel"
    }

    enum class TargetTab {
        UNKNOWN,
        TASK_LIST_SCREEN,
        CUSTOMER_LIST_SCREEN,
        WEIGHING_LIST_SCREEN,
        REPORT_SCREEN,
        PROFILE_SCREEN
    }

    data class State(
        val uiNotificationMessage: String?,
        val progressBarVisibilityStatus: Boolean,
        val qrCodeScannerVisibilityStatus: Boolean,
        val targetTab: TargetTab
    )

    var state by mutableStateOf(
        State(
            uiNotificationMessage = null,
            progressBarVisibilityStatus = false,
            qrCodeScannerVisibilityStatus = false,
            targetTab = TargetTab.UNKNOWN
        )
    )
        private set

    enum class Event {
        UNKNOWN,
        GET_SIGNATURE,
        GET_PHOTO,
        GET_QR_CODE,
        LOG_OUT,
        LAUNCH_CUSTOMER_DETAILS,
        LAUNCH_WEIGHING_DETAILS
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
        serveConnectivityObserver()
        serveTaskListScreen()
        serveCustomerListScreen()
        serveWeighingListScreen()
        serveReportScreen()
        serveProfileScreen()
    }

    private fun serveStomp() {
        viewModelScope.launch {
            repositoryLocal.getStompEntityAsFlow().collect { stompEntity ->
                startStomp(stompEntity)
            }
        }

        viewModelScope.launch {
            startStomp(repositoryLocal.getStompEntity())
        }

        viewModelScope.launch {
            listenStomp()
        }
    }

    private fun startStomp(stompEntity: StompEntity?) {
        try {
            stompEntity?.let {
                stompManager.start(it.mapToStompConnectionDetails())
                return
            }
            //setUiNotification(Strings.FAILED_TO_START_STOMP_CONNECTION_NO_CONNECTION_DETAILS)
            Logger.d(
                TAG,
                "Failed to start STOMP connection: No connection details. It is ok for first app launch"
            )
        } catch (e: Exception) {
            setUiNotification(Strings.FAILED_TO_START_STOMP_ERROR + e.message)
            Logger.d(TAG, "Failed to start STOMP. Error: ${e.message}")
        }
    }

    private suspend fun listenStomp() {
        stompManager.collectStompData { stompMessage ->
            Logger.d(
                TAG,
                "stompManager.collectStompData:" +
                        " subscriberTag = ${stompMessage.subscriberTag}," +
                        " message = ${stompMessage.message}\""
            )

            val stompSubscriberTag = stompMessage.subscriberTag
            val revealedMessageList = stompMessage.message.split(StompMessage.SEPARATOR)

            when(stompSubscriberTag){
                TaskSubscriber.TAG -> {
                    if(revealedMessageList[0] == StompMessage.NAV_X_MESSAGE_SIGNATURE){
                        if(revealedMessageList.size == 8) {
                            Logger.d(TAG, "message from Nav-x with TR-727")
                            val taskEntity = TaskEntity(
                                type = revealedMessageList[1].toInt(),
                                customerId = revealedMessageList[2],
                                deviceId = revealedMessageList[3],
                                liftId = revealedMessageList[4],
                                timestamp = revealedMessageList[5],
                                reasonId = revealedMessageList[6],
                                customerRefId = revealedMessageList[7]
                            )
                            repositoryLocal.insert(taskEntity)
                        }else if(revealedMessageList.size == 7){
                            Logger.d(TAG, "message from Nav-x without TR-727")
                            val customerId = revealedMessageList[2]
                            val customerRefId = repositoryLocal
                                .getByCustomerId(customerId.toInt())?.customerRefId ?: ""
                            val taskEntity = TaskEntity(
                                type = revealedMessageList[1].toInt(),
                                customerId = customerId,
                                deviceId = revealedMessageList[3],
                                liftId = revealedMessageList[4],
                                timestamp = revealedMessageList[5],
                                reasonId = revealedMessageList[6],
                                customerRefId = customerRefId
                            )
                            repositoryLocal.insert(taskEntity)
                        }else{
                            Logger.d(TAG, "Unexpected message size")
                        }
                    }
                }

                RouteSubscriber.TAG -> {
                    if(revealedMessageList[0] == StompMessage.NAV_X_MESSAGE_SIGNATURE){
                        repositoryLocal.clearTaskEntity()
                        getCustomerList()
                    }
                }
            }
        }
    }

    fun serveConnectivityObserver() {
        ConnectivityObserver().create(onConnectionStateChanged = { onlineStatus ->
            Logger.d(TAG, "connectivityObserver.isOnline.collect: $onlineStatus")
            if (onlineStatus) {
                viewModelScope.launch {
                    http.synchronizeOfflineData()
                }
            }
        })
    }

    fun serveCameraScreen(taskTag: String, photoPath: String) {
        viewModelScope.launch {
            when (taskTag) {
                TargetTab.TASK_LIST_SCREEN.name -> {
                    taskListViewModel.state.selectedTaskEntity?.let {
                        updateTask(it, photoPath)
                    }
                }

                TargetTab.CUSTOMER_LIST_SCREEN.name -> {
                    customerListViewModel.state.createdTaskEntity?.let {
                        updateTask(it, photoPath)
                    }
                }

                TargetTab.REPORT_SCREEN.name -> {
                    reportViewModel.state.selectedTruckReportEntity?.let {
                        reportViewModel.updateTruckReportEntityPhotoPath(it, photoPath)
                    }
                }

                else -> {}
            }
        }
    }

    fun serveDrawingPadScreen(taskTag: String, photoPath: String) {
        viewModelScope.launch {
            when (taskTag) {
                TargetTab.TASK_LIST_SCREEN.name -> {
                    taskListViewModel.state.selectedTaskEntity?.let {
                        updateTask(it, photoPath)
                    }
                }

                TargetTab.CUSTOMER_LIST_SCREEN.name -> {
                    customerListViewModel.state.createdTaskEntity?.let {
                        updateTask(it, photoPath)
                    }
                }

                else -> {}
            }
        }
    }

    private fun serveTaskListScreen() {
        viewModelScope.launch {
            taskListViewModel.collectEvent { event ->
                state = state.copy(targetTab = TargetTab.TASK_LIST_SCREEN)

                when (event) {
                    TaskListViewModel.Event.GET_SIGNATURE -> {
                        emitEvent(Event.GET_SIGNATURE)
                    }

                    TaskListViewModel.Event.GET_PHOTO -> {
                        emitEvent(Event.GET_PHOTO)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun serveCustomerListScreen() {
        viewModelScope.launch {
            customerListViewModel.collectEvent { event ->
                state = state.copy(targetTab = TargetTab.CUSTOMER_LIST_SCREEN)

                when (event) {
                    CustomerListViewModel.Event.GET_SIGNATURE -> {
                        emitEvent(Event.GET_SIGNATURE)
                    }

                    CustomerListViewModel.Event.GET_PHOTO -> {
                        emitEvent(Event.GET_PHOTO)
                    }

                    CustomerListViewModel.Event.GET_QR_CODE -> {
                        emitEvent(Event.GET_QR_CODE)
                    }

                    CustomerListViewModel.Event.SHOW_CUSTOMER_DETAILS -> {
                        customerListViewModel.state.selectedCustomerEntity?.let {
                            customerDetailsViewModel.setCustomerEntity(it)
                            emitEvent(Event.LAUNCH_CUSTOMER_DETAILS)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun serveWeighingListScreen() {
        viewModelScope.launch {
            weighingListViewModel.collectEvent { event ->
                state = state.copy(targetTab = TargetTab.WEIGHING_LIST_SCREEN)

                when (event) {
                    WeighingListViewModel.Event.SHOW_WEIGHING_DETAILS -> {
                        weighingListViewModel.state.selectedJobEntity?.let {
                            weighingDetailsViewModel.setJobEntity(it)
                            emitEvent(Event.LAUNCH_WEIGHING_DETAILS)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun serveReportScreen() {
        viewModelScope.launch {
            reportViewModel.collectEvent { event ->
                state = state.copy(targetTab = TargetTab.REPORT_SCREEN)
                when (event) {
                    ReportViewModel.Event.GET_PHOTO -> {
                        emitEvent(Event.GET_PHOTO)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun serveProfileScreen() {
        viewModelScope.launch {
            profileViewModel.collectEvent { event ->
                state = state.copy(targetTab = TargetTab.PROFILE_SCREEN)
                when (event) {
                    ProfileViewModel.Event.LOG_OUT -> {
                        emitEvent(Event.LOG_OUT)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun updateTask(taskEntity: TaskEntity, filePath: String) {
        viewModelScope.launch {
            taskEntity.filePath = filePath
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
                                HttpStatus.Started -> {
                                    setProgressBarState(true)
                                }

                                HttpStatus.Completed -> {
                                    if (error != null) {
                                        setUiNotification(Strings.PHOTO_NOT_SENT + "/n" + error)
                                        taskEntity.status = TaskEntity.PENDING_STATUS
                                        repositoryLocal.update(taskEntity)
                                    } else {
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
                                HttpStatus.Started -> {
                                    setProgressBarState(true)
                                }

                                HttpStatus.Completed -> {
                                    if (error != null) {
                                        setUiNotification(Strings.SIGNATURE_NOT_SENT + "/n" + error)
                                        taskEntity.status = TaskEntity.PENDING_STATUS
                                        repositoryLocal.update(taskEntity)
                                    } else {
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

    fun manageQrCodeScanner(show: Boolean, qrCode: String? = null) {
        state = state.copy(qrCodeScannerVisibilityStatus = show)

        when (state.targetTab) {
            TargetTab.CUSTOMER_LIST_SCREEN -> {
                qrCode?.let {
                    customerListViewModel.setQrCode(it)
                }
            }

            else -> {}
        }
    }

    private suspend fun getTasksIdsString(customerId: String?) =
        customerId?.let { nnCustomerId ->
            repositoryLocal.getCustomerTaskEntityByCustomerIdList(nnCustomerId)
                ?.joinToString(separator = ",") {
                    it.taskId
                }
        }

    fun getCustomerList() {
        viewModelScope.launch {
            http.getCustomerList(
                onEvent = { status, data, error ->
                    when (status) {
                        HttpStatus.Started -> {
                            setProgressBarState(true)
                        }

                        HttpStatus.Completed -> {
                            data?.let { customerResponseList ->
                                customerResponseList.customers?.let {
                                    repositoryLocal.clearCustomerTable()
                                    repositoryLocal.insertCustomerEntityList(it.map { customerResponse -> customerResponse.mapToCustomerEntity() })
                                }
                            }
                            error?.let {
                                if((error.message ?: "").contains("Route id is empty")){
                                    repositoryLocal.clearCustomerTable()
                                }else {
                                    setUiNotification(error.message)
                                }
                            }
                            setProgressBarState(false)

                            getCustomerTaskList()
                        }

                        else -> {}
                    }
                }
            )
        }
    }

    suspend fun getCustomerTaskList() {
        http.getCustomerTaskList(
            onEvent = { status, data, error ->
                when (status) {
                    HttpStatus.Started -> {
                        setProgressBarState(true)
                    }

                    HttpStatus.Completed -> {
                        if (error != null) {
                            if ((error.message ?: "").contains("Route id is empty")) {
                                repositoryLocal.clearCustomerTable()
                            } else {
                                setUiNotification(error.message)
                            }
                        }else{
                            data?.let { customerTaskListResponse ->
                                customerTaskListResponse.tasks.let { customerTaskResponseList ->
                                    customerTaskResponseList?.let { nnCustomerTaskResponseList ->
                                        repositoryLocal.clearCustomerTaskEntityList()
                                        repositoryLocal.insertCustomerTaskEntityList(
                                            nnCustomerTaskResponseList.map {
                                                it.mapToCustomerTaskEntity()
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        setProgressBarState(false)

                        getCurrentRoute()
                    }

                    else -> {}
                }
            }
        )
    }

    fun getCurrentRoute() {
        viewModelScope.launch {
            http.getCurrentRoute (
                onEvent = { status, data, error ->
                    when (status) {
                        HttpStatus.Started -> {
                            setProgressBarState(true)
                        }

                        HttpStatus.Completed -> {
                            val authorizationEntity = repositoryLocal
                                .getAuthorizationEntity() ?: AuthorizationEntity(
                                id = -1,
                                rosterId = -1,
                                routeId = -1,
                                routeName = "",
                                sessionKey = "",
                                userId = "",
                                agentType = "",
                                name = "",
                                userName = "",
                                mq = "",
                                securityPass = "",
                                truckId = -1,
                                truckReg = "",
                                truckUserId = "",
                                truckHash = ""
                            )
                            data?.let { routeResponse ->
                                routeResponse.RouteData?.let { route ->
                                    repositoryLocal.update(
                                        authorizationEntity.copy(
                                            rosterId = route.TruckRosterId,
                                            routeId = route.RouteId,
                                            routeName = route.RouteName)
                                    )
                                }
                            }
                            error?.let {
                                if((error.message ?: "").contains("Route id is empty")){
                                    repositoryLocal.update(
                                        authorizationEntity.copy(
                                            rosterId = -1,
                                            routeId = -1,
                                            routeName = ""
                                        )
                                    )
                                }else {
                                    setUiNotification(error.message)
                                }
                            }
                            setProgressBarState(false)
                        }

                        else -> {}
                    }
                }
            )
        }
    }
}