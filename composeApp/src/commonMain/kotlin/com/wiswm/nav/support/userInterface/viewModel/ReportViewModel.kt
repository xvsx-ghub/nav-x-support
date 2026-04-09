package com.wiswm.nav.support.userInterface.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.dataBase.entity.TruckReportEntity
import com.wiswm.nav.support.data.remote.http.Http
import com.wiswm.nav.support.data.remote.http.HttpClientCore
import com.wiswm.nav.support.resources.Strings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ReportViewModel(
    private val repositoryLocal: RepositoryLocal,
    private val http: Http
) : ViewModel() {

    companion object {
        const val TAG = "ReportViewModel"
    }

    data class State(
        val updateUiTrigger: Boolean,
        val uiNotificationMessage: String?,
        val progressBarVisibilityStatus: Boolean,
        val oneButtonAlertDialogVisibilityStatus: Boolean,
        val twoButtonsAlertDialogVisibilityStatus: Boolean,
        val truckReportEntityList: List<TruckReportEntity>?,
        val selectedTruckReportEntity: TruckReportEntity?
    )

    var state by mutableStateOf(
        State(
            updateUiTrigger = false,
            uiNotificationMessage = null,
            progressBarVisibilityStatus = false,
            oneButtonAlertDialogVisibilityStatus = false,
            twoButtonsAlertDialogVisibilityStatus = false,
            truckReportEntityList = null,
            selectedTruckReportEntity = null
        )
    )
        private set

    enum class Event {
        UNKNOWN,
        GET_PHOTO,
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
        refreshTruckReportList()
    }

    private fun refreshTruckReportList() {
        viewModelScope.launch {
            repositoryLocal.getTruckReportEntityList()?.let {
                state =
                    state.copy(truckReportEntityList = it, updateUiTrigger = !state.updateUiTrigger)
            }
        }
    }

    fun updateTruckReportEntityCheckedStatus(
        truckReportEntity: TruckReportEntity,
        checkedStatus: Boolean
    ) {
        val tmpTruckReportEntityList = state.truckReportEntityList?.toMutableList()
        tmpTruckReportEntityList?.let { nnTmpTruckReportEntityList ->
            val index = nnTmpTruckReportEntityList.indexOfFirst { it.id == truckReportEntity.id }
            if (index != -1) {
                nnTmpTruckReportEntityList[index] =
                    nnTmpTruckReportEntityList[index].copy(checkedStatus = checkedStatus)
                state = state.copy(truckReportEntityList = nnTmpTruckReportEntityList)
            }
        }
    }

    fun updateTruckReportEntityValue(
        truckReportEntity: TruckReportEntity,
        value: String
    ) {
        val tmpTruckReportEntityList = state.truckReportEntityList?.toMutableList()
        tmpTruckReportEntityList?.let { nnTmpTruckReportEntityList ->
            val index = nnTmpTruckReportEntityList.indexOfFirst { it.id == truckReportEntity.id }
            if (index != -1) {
                nnTmpTruckReportEntityList[index] =
                    nnTmpTruckReportEntityList[index].copy(value = value)
                state = state.copy(truckReportEntityList = nnTmpTruckReportEntityList)
            }
        }
    }

    fun updateTruckReportEntityPhotoPath(
        truckReportEntity: TruckReportEntity,
        photoPath: String
    ) {
        val tmpTruckReportEntityList = state.truckReportEntityList?.toMutableList()
        tmpTruckReportEntityList?.let { nnTmpTruckReportEntityList ->
            val index = nnTmpTruckReportEntityList.indexOfFirst { it.id == truckReportEntity.id }
            if (index != -1) {
                nnTmpTruckReportEntityList[index] =
                    nnTmpTruckReportEntityList[index].copy(photoPath = photoPath)
                state = state.copy(truckReportEntityList = nnTmpTruckReportEntityList)
            }
        }
    }

    //TODO Probably the photo file should be deleted from device storage as well.
    fun deleteTruckReportEntityPhotoPath(
        truckReportEntity: TruckReportEntity
    ) {
        val tmpTruckReportEntityList = state.truckReportEntityList?.toMutableList()
        tmpTruckReportEntityList?.let { nnTmpTruckReportEntityList ->
            val index = nnTmpTruckReportEntityList.indexOfFirst { it.id == truckReportEntity.id }
            if (index != -1) {
                nnTmpTruckReportEntityList[index] =
                    nnTmpTruckReportEntityList[index].copy(photoPath = null)
                state = state.copy(truckReportEntityList = nnTmpTruckReportEntityList)
            }
        }
    }

    fun setSelectedTruckReportEntity(truckReportEntity: TruckReportEntity) {
        state = state.copy(selectedTruckReportEntity = truckReportEntity)
    }

    fun getPhoto() {
        emitEvent(Event.GET_PHOTO)
    }

    fun pushOneButtonAlertDialog(visibilityStatus: Boolean) {
        state = state.copy(oneButtonAlertDialogVisibilityStatus = visibilityStatus)
    }

    fun pushTwoButtonsAlertDialog(visibilityStatus: Boolean) {
        state = state.copy(twoButtonsAlertDialogVisibilityStatus = visibilityStatus)
    }

    fun sendTruckReport() {
        viewModelScope.launch {
            repositoryLocal.getAuthorizationEntity()?.let { authorizationEntity ->
                state.truckReportEntityList?.let { truckReportEntityList ->
                    http.setTruckReport(
                        truckNumber = authorizationEntity.truckReg,
                        driverId = authorizationEntity.userId,
                        truckReportEntityList = truckReportEntityList,
                        onEvent = { status, data, error ->
                            when (status) {
                                HttpClientCore.HttpStatus.Started -> {
                                    setProgressBarState(true)
                                }

                                HttpClientCore.HttpStatus.Completed -> {
                                    data?.let { message ->
                                        refreshTruckReportList()
                                        pushOneButtonAlertDialog(true)
                                    }
                                    error?.let {
                                        setUiNotification(Strings.REPORT_NOT_SENT)
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
    }

    fun getTruckReportList() {
        viewModelScope.launch {
            http.getTruckReportList(
                onEvent = { status, data, error ->
                    when (status) {
                        HttpClientCore.HttpStatus.Started -> {
                            setProgressBarState(true)
                        }

                        HttpClientCore.HttpStatus.Completed -> {
                            data?.let {
                                repositoryLocal.clearTruckReportEntityList()
                                repositoryLocal.insertTruckReportEntityList(
                                    it.TruckReportParams.map { notServicingReasonResponse ->
                                        notServicingReasonResponse.mapToTruckReportEntity(
                                            value = null,
                                            photoPath = null,
                                            checkedStatus = false
                                        )
                                    }
                                )
                                refreshTruckReportList()
                            }
                            error?.let {
                                setUiNotification(it.message)
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