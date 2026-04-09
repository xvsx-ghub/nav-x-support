package com.wiswm.nav.support.userInterface.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.dataBase.entity.AuthorizationEntity
import com.wiswm.nav.support.data.local.dataBase.entity.CustomerEntity
import com.wiswm.nav.support.data.local.dataBase.entity.NotServicingReasonEntity
import com.wiswm.nav.support.data.local.dataBase.entity.TaskEntity
import com.wiswm.nav.support.data.remote.http.Http
import com.wiswm.nav.support.data.remote.http.HttpClientCore.HttpStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class CustomerListViewModel(
    val http: Http,
    val repositoryLocal: RepositoryLocal
) : ViewModel() {
    companion object {
        const val TAG = "CustomerListViewModel"
    }

    data class State(
        val routeName: String?,
        val customerEntityList: List<CustomerEntity>?,
        val selectedCustomerEntity: CustomerEntity?,
        val notServicingReasonEntityList: List<NotServicingReasonEntity>?,
        val uiNotificationMessage: String?,
        val progressBarVisibilityStatus: Boolean,
        val notServicingReasonAlertDialogVisibilityStatus: Boolean,
        val searchActiveStatus: Boolean,
        val qrCode: String?,
        val createdTaskEntity: TaskEntity?
    )

    var state by mutableStateOf(
        State(
            routeName = null,
            customerEntityList = null,
            selectedCustomerEntity = null,
            notServicingReasonEntityList = null,
            uiNotificationMessage = null,
            progressBarVisibilityStatus = false,
            notServicingReasonAlertDialogVisibilityStatus = false,
            searchActiveStatus = false,
            qrCode = null,
            createdTaskEntity = null
        )
    )
        private set

    enum class Event {
        UNKNOWN,
        GET_SIGNATURE,
        GET_PHOTO,
        GET_QR_CODE,
        SHOW_CUSTOMER_DETAILS
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

    fun setProgressBarState(visible: Boolean) {
        state = state.copy(progressBarVisibilityStatus = visible)
    }

    fun setUiNotification(message: String?) {
        state = state.copy(uiNotificationMessage = message)
        viewModelScope.launch {
            delay(1000)
            state = state.copy(uiNotificationMessage = null)
        }
    }

    init {
        serveRoute()
        serveCustomerList()
        serveNotServicingReasonList()
    }

    private fun serveRoute() {
        viewModelScope.launch {
            repositoryLocal.getAuthorizationEntity()?.let {
                state = state.copy(routeName = it.routeName)
            }
        }

        viewModelScope.launch {
            repositoryLocal.getAuthorizationEntityAsFlow().collect {
                it?.let {
                    state = state.copy(routeName = it.routeName)
                }
            }
        }
    }

    private fun serveCustomerList() {
        viewModelScope.launch {
            repositoryLocal.getCustomerList()?.let {
                state = state.copy(customerEntityList = it)
            }
        }

        viewModelScope.launch {
            repositoryLocal.getCustomerListAsFlow().collect {
                state = state.copy(customerEntityList = it)
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
                                if ((error.message ?: "").contains("Route id is empty")) {
                                    repositoryLocal.clearCustomerTable()
                                } else {
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
            http.getCurrentRoute(
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
                                            routeName = route.RouteName
                                        )
                                    )
                                }
                            }
                            error?.let {
                                if ((error.message ?: "").contains("Route id is empty")) {
                                    repositoryLocal.update(
                                        authorizationEntity.copy(
                                            rosterId = -1,
                                            routeId = -1,
                                            routeName = ""
                                        )
                                    )
                                } else {
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

    enum class SearchType {
        Id,
        Address,
        All
    }

    private fun determinateSearchType(text: String): SearchType {
        if (text.isEmpty()) {
            return SearchType.All
        } else {
            return when (text.toIntOrNull()) {
                null -> SearchType.Address
                else -> SearchType.Id
            }
        }
    }

    fun updateCustomerList(text: String) {
        viewModelScope.launch {
            val searchType = determinateSearchType(text)

            when (searchType) {
                SearchType.Id -> {
                    state = state.copy(
                        customerEntityList = repositoryLocal.getSimilarByCustomerRefId(text.toInt()),
                        searchActiveStatus = true
                    )
                }

                SearchType.Address -> {
                    state = state.copy(
                        customerEntityList = repositoryLocal.getSimilarByAddress(text),
                        searchActiveStatus = true
                    )
                }

                SearchType.All -> {
                    state = state.copy(
                        customerEntityList = repositoryLocal.getCustomerList(),
                        searchActiveStatus = false
                    )
                }
            }
        }
    }

    fun getSignature() {
        emitEvent(Event.GET_SIGNATURE)
    }

    fun getPhoto() {
        emitEvent(Event.GET_PHOTO)
    }

    fun getQrCode() {
        emitEvent(Event.GET_QR_CODE)
    }

    fun setQrCode(qrCode: String?) {
        state = state.copy(qrCode = qrCode)
    }

    fun pushNotServicingReasonAlertDialog(show: Boolean) {
        state = state.copy(notServicingReasonAlertDialogVisibilityStatus = show)
    }

    fun setSelectedCustomerEntity(customerEntity: CustomerEntity) {
        state = state.copy(selectedCustomerEntity = customerEntity)
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

    fun createSignatureTask() {
        viewModelScope.launch {
            state.selectedCustomerEntity?.let { nnSelectedCustomerEntity ->
                val userId = repositoryLocal.getAuthorizationEntity()?.userId
                val truckId = repositoryLocal.getAuthorizationEntity()?.truckId
                val crewId = "$userId$truckId"
                val taskEntity = TaskEntity(
                    type = TaskEntity.TAKE_SIGNATURE_TYPE,
                    customerId = nnSelectedCustomerEntity.customerId.toString(),
                    customerRefId = nnSelectedCustomerEntity.customerRefId,
                    liftId = repositoryLocal.getLocalGeneratedTransactionId().toString(),
                    deviceId = crewId,
                    timestamp = repositoryLocal.getFormattedCurrentTimeSeconds(),
                    reasonId = "",
                    status = TaskEntity.NEW_STATUS,
                    filePath = ""
                )
                val id = repositoryLocal.insert(taskEntity)
                val dbTaskEntity = repositoryLocal.getTaskEntity(id)
                state = state.copy(createdTaskEntity = dbTaskEntity)
                getSignature()
            }
        }
    }

    fun showCustomerDetails() {
        emitEvent(Event.SHOW_CUSTOMER_DETAILS)
    }
}