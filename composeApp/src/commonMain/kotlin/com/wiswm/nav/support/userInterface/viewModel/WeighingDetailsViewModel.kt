package com.wiswm.nav.support.userInterface.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.dataBase.entity.DestinationEntity
import com.wiswm.nav.support.data.local.dataBase.entity.JobEntity
import com.wiswm.nav.support.data.local.dataBase.entity.WasteTypeEntity
import com.wiswm.nav.support.data.local.dataBase.entity.Weighbridge
import com.wiswm.nav.support.data.local.dataBase.entity.Weighing
import com.wiswm.nav.support.data.remote.http.Http
import com.wiswm.nav.support.data.remote.http.HttpClientCore
import com.wiswm.nav.support.data.remote.http.response.WeighbridgeResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class WeighingDetailsViewModel(
    private val repositoryLocal: RepositoryLocal,
    val http: Http
) : ViewModel() {
    companion object {
        const val TAG = "WeighingDetailsViewModel"
    }

    data class State(
        val uiNotificationMessage: String?,
        val progressBarVisibilityStatus: Boolean,
        val jobEntity: JobEntity?,
        val weighing: Weighing?,
        val destinationEntityList: List<DestinationEntity>?,
        val wasteTypeEntityList: List<WasteTypeEntity>?,
        val dataValidationStatus: Boolean,
        val selectedDestinationEntity: DestinationEntity?,
        val selectedWeighbridge: Weighbridge?,
        val selectedWasteTypeEntity: WasteTypeEntity?,
        val weightSectionVisibilityStatus: Boolean,
        val weighbridgeSectionVisibilityStatus: Boolean,
        val photoPath: String?,
        val photoRequiredDialogVisibilityStatus: Boolean,
        val confirmationDialogVisibilityStatus: Boolean,
        val successDialogVisibilityStatus: Boolean,
        val errorDialogVisibilityStatus: Boolean,
        val destinationSpinnerEnableStatus: Boolean,
        val weighbridgeSpinnerEnableStatus: Boolean,
        val wasteTypeSpinnerEnableStatus: Boolean,
        val refreshButtonBusyStatus: Boolean
        )

    var state by mutableStateOf(
        State(
            uiNotificationMessage = null,
            progressBarVisibilityStatus = false,
            jobEntity = null,
            weighing = null,
            destinationEntityList = null,
            wasteTypeEntityList = null,
            dataValidationStatus = false,
            selectedDestinationEntity = null,
            selectedWeighbridge = null,
            selectedWasteTypeEntity = null,
            weightSectionVisibilityStatus = false,
            weighbridgeSectionVisibilityStatus = false,
            photoPath = null,
            photoRequiredDialogVisibilityStatus = false,
            confirmationDialogVisibilityStatus = false,
            successDialogVisibilityStatus = false,
            errorDialogVisibilityStatus = false,
            destinationSpinnerEnableStatus = true,
            weighbridgeSpinnerEnableStatus = true,
            wasteTypeSpinnerEnableStatus = true,
            refreshButtonBusyStatus = false
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

    init {
        serveDestinationData()
        serveWasteTypeData()
    }

    private fun serveDestinationData() {
        viewModelScope.launch {
            state = state.copy(destinationEntityList = repositoryLocal.getDestinationEntityList())

            repositoryLocal.getDestinationEntityListAsFlow().collect { destinationEntityList ->
                state = state.copy(destinationEntityList = destinationEntityList)
            }
        }
    }

    private fun serveWasteTypeData() {
        viewModelScope.launch {
            state = state.copy(wasteTypeEntityList = repositoryLocal.getWasteTypeEntityList())

            repositoryLocal.getWasteTypeEntityListAsFlow().collect { wasteTypeEntityList ->
                state = state.copy(wasteTypeEntityList = wasteTypeEntityList)
            }
        }
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

    fun setJobEntity(jobEntity: JobEntity) {
        if (
            jobEntity.type == JobEntity.Type.InAndOut
            && (jobEntity.weighingIn != null && jobEntity.weighingOut == null)
        ) {
            val destination = state.destinationEntityList?.find { destinationEntity ->
                destinationEntity.remoteId == jobEntity.weighingIn.destinationId
            }
            val weighbridge = destination?.weighbridgeList?.find { weighbridge ->
                weighbridge.remoteId == jobEntity.weighingIn.weighbridgeId
            }
            val wasteType = state.wasteTypeEntityList?.find { wasteTypeEntity ->
                wasteTypeEntity.remoteId == jobEntity.weighingIn.wasteTypeId
            }

            state = state.copy(
                uiNotificationMessage = null,
                progressBarVisibilityStatus = false,
                jobEntity = jobEntity,
                weighing = Weighing(
                    remoteId = jobEntity.weighingIn.remoteId,
                    destinationId = jobEntity.weighingIn.destinationId,
                    weighbridgeId = jobEntity.weighingIn.weighbridgeId,
                    wasteTypeId = jobEntity.weighingIn.wasteTypeId,
                    weightValue = null,
                    timestamp = null,
                    type = jobEntity.weighingIn.type,
                    direction = Weighing.Direction.Out,
                    remoteErrorMessage = null,
                    docketPhoto = null
                ),
                dataValidationStatus = false,
                selectedDestinationEntity = destination,
                selectedWeighbridge = weighbridge,
                selectedWasteTypeEntity = wasteType,
                weightSectionVisibilityStatus = true,
                weighbridgeSectionVisibilityStatus = true,
                photoPath = null,
                photoRequiredDialogVisibilityStatus = false,
                confirmationDialogVisibilityStatus = false,
                successDialogVisibilityStatus = false,
                errorDialogVisibilityStatus = false,
                destinationSpinnerEnableStatus = false,
                weighbridgeSpinnerEnableStatus = false,
                wasteTypeSpinnerEnableStatus = false
            )
        } else {
            state = state.copy(
                jobEntity = jobEntity, weighing = Weighing(
                    remoteId = null,
                    destinationId = null,
                    weighbridgeId = null,
                    wasteTypeId = null,
                    weightValue = null,
                    timestamp = null,
                    type = null,
                    direction = null,
                    remoteErrorMessage = null,
                    docketPhoto = null
                )
            )
        }
    }

    fun submitWeighing() {
        viewModelScope.launch {
            state.weighing?.let { weighing ->
                state.jobEntity?.let { jobEntity ->
                    val timestamp = repositoryLocal.getFormattedCurrentTimeSeconds()
                    val authorizationEntity = repositoryLocal.getAuthorizationEntity()
                    var successStatus = false

                    when (weighing.direction) {
                        Weighing.Direction.In -> {
                            val newJobEntity =
                                jobEntity.copy(weighingIn = weighing.copy(timestamp = timestamp))
                            state = state.copy(jobEntity = newJobEntity)
                            repositoryLocal.updateJobEntity(newJobEntity)

                            http.setWeightIn(
                                wasteTypeId = newJobEntity.weighingIn?.wasteTypeId ?: "",
                                destinationId = newJobEntity.weighingIn?.destinationId ?: "",
                                weighbridgeId = newJobEntity.weighingIn?.weighbridgeId ?: "",
                                weight = newJobEntity.weighingIn?.weightValue ?: "",
                                timestamp = newJobEntity.weighingIn?.timestamp ?: "",
                                driverId = authorizationEntity?.userId ?: "",
                                routeId = authorizationEntity?.routeId.toString(),
                                truckId = authorizationEntity?.truckId.toString(),
                                onEvent = { status, data, error ->
                                    when (status) {
                                        HttpClientCore.HttpStatus.Started -> {
                                            pushProgressBar(true)
                                        }

                                        HttpClientCore.HttpStatus.Completed -> {
                                            data?.let { weighingResponse ->
                                                weighingResponse.WeighingId?.let {
                                                    val updatedNewJobEntity = newJobEntity.copy(
                                                        completedStatus = newJobEntity.type == JobEntity.Type.In,
                                                        weighingIn = newJobEntity.weighingIn?.copy(
                                                            remoteId = it.toString()
                                                        )
                                                    )
                                                    state =
                                                        state.copy(jobEntity = updatedNewJobEntity)
                                                    repositoryLocal.updateJobEntity(
                                                        updatedNewJobEntity
                                                    )
                                                    successStatus = true
                                                }

                                                weighingResponse.ErrorMessage?.let {
                                                    val updatedNewJobEntity = newJobEntity.copy(
                                                        weighingIn = newJobEntity.weighingIn?.copy(
                                                            remoteErrorMessage = it
                                                        )
                                                    )
                                                    state =
                                                        state.copy(jobEntity = updatedNewJobEntity)
                                                    repositoryLocal.updateJobEntity(
                                                        updatedNewJobEntity
                                                    )
                                                    successStatus = false
                                                }
                                            }
                                            error?.let {
                                                successStatus = false
                                            }
                                            pushProgressBar(false)
                                            if (successStatus) pushSuccessDialog(true)
                                            else pushErrorDialog(true)
                                        }

                                        else -> {}
                                    }
                                }
                            )
                        }

                        Weighing.Direction.Out -> {
                            val newJobEntity =
                                jobEntity.copy(weighingOut = weighing.copy(timestamp = timestamp))
                            state = state.copy(jobEntity = newJobEntity)
                            repositoryLocal.updateJobEntity(newJobEntity)

                            http.setWeightOut(
                                weighingId = newJobEntity.weighingIn?.remoteId ?: "",
                                weighbridgeId = newJobEntity.weighingOut?.wasteTypeId ?: "",
                                timestamp = newJobEntity.weighingOut?.timestamp ?: "",
                                weight = newJobEntity.weighingOut?.weightValue ?: "",
                                onEvent = { status, data, error ->
                                    when (status) {
                                        HttpClientCore.HttpStatus.Started -> {
                                            pushProgressBar(true)
                                        }

                                        HttpClientCore.HttpStatus.Completed -> {
                                            data?.let { weighingResponse ->
                                                weighingResponse.WeighingId?.let {
                                                    val updatedNewJobEntity = newJobEntity.copy(
                                                        completedStatus = newJobEntity.type == JobEntity.Type.Out || newJobEntity.type == JobEntity.Type.InAndOut,
                                                        weighingOut = newJobEntity.weighingOut?.copy(
                                                            remoteId = it.toString()
                                                        )
                                                    )
                                                    state =
                                                        state.copy(jobEntity = updatedNewJobEntity)
                                                    repositoryLocal.updateJobEntity(
                                                        updatedNewJobEntity
                                                    )
                                                    successStatus = true
                                                }

                                                weighingResponse.ErrorMessage?.let {
                                                    val updatedNewJobEntity = newJobEntity.copy(
                                                        weighingOut = newJobEntity.weighingOut?.copy(
                                                            remoteErrorMessage = it
                                                        )
                                                    )
                                                    state =
                                                        state.copy(jobEntity = updatedNewJobEntity)
                                                    repositoryLocal.updateJobEntity(
                                                        updatedNewJobEntity
                                                    )
                                                    successStatus = false
                                                }
                                            }
                                            error?.let {
                                                successStatus = false
                                            }
                                            pushProgressBar(false)
                                            if (successStatus) pushSuccessDialog(true)
                                            else pushErrorDialog(true)
                                        }

                                        else -> {}
                                    }
                                }
                            )
                        }

                        Weighing.Direction.Net -> {
                            val newJobEntity =
                                jobEntity.copy(weighingNet = weighing.copy(timestamp = timestamp))
                            state = state.copy(jobEntity = newJobEntity)
                            repositoryLocal.updateJobEntity(newJobEntity)

                            http.setWeightNet(
                                wasteTypeId = newJobEntity.weighingNet?.wasteTypeId ?: "",
                                destinationId = newJobEntity.weighingNet?.destinationId ?: "",
                                weightIn = newJobEntity.weighingNet?.weightValue
                                    ?: "",             //? maybe 0
                                timestampIn = newJobEntity.weighingNet?.timestamp ?: "",
                                weightOut = newJobEntity.weighingNet?.weightValue ?: "",
                                driverId = authorizationEntity?.userId ?: "",
                                routeId = authorizationEntity?.routeId.toString(),
                                truckId = authorizationEntity?.truckId.toString(),
                                onEvent = { status, data, error ->
                                    when (status) {
                                        HttpClientCore.HttpStatus.Started -> {
                                            pushProgressBar(true)
                                        }

                                        HttpClientCore.HttpStatus.Completed -> {
                                            data?.let { weighingResponse ->
                                                weighingResponse.WeighingId?.let {
                                                    val updatedNewJobEntity = newJobEntity.copy(
                                                        completedStatus = newJobEntity.type == JobEntity.Type.Net,
                                                        weighingNet = newJobEntity.weighingNet?.copy(
                                                            remoteId = it.toString()
                                                        )
                                                    )
                                                    state =
                                                        state.copy(jobEntity = updatedNewJobEntity)
                                                    repositoryLocal.updateJobEntity(
                                                        updatedNewJobEntity
                                                    )
                                                    successStatus = true
                                                }

                                                weighingResponse.ErrorMessage?.let {
                                                    val updatedNewJobEntity = newJobEntity.copy(
                                                        weighingNet = newJobEntity.weighingNet?.copy(
                                                            remoteErrorMessage = it
                                                        )
                                                    )
                                                    state =
                                                        state.copy(jobEntity = updatedNewJobEntity)
                                                    repositoryLocal.updateJobEntity(
                                                        updatedNewJobEntity
                                                    )
                                                    successStatus = false
                                                }
                                            }
                                            error?.let {
                                                successStatus = false
                                            }
                                            pushProgressBar(false)
                                            if (successStatus) pushSuccessDialog(true)
                                            else pushErrorDialog(true)
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
        }
    }

    fun refreshWeight(onRefreshStarted: () -> Unit, onRefreshCompleted: () -> Unit, onRefreshBusy: () -> Unit) {
        if(state.refreshButtonBusyStatus){
            onRefreshBusy()
            return
        }

        viewModelScope.launch {
            state = state.copy(refreshButtonBusyStatus = true)
            onRefreshStarted()
            delay(5000)
            state = state.copy(refreshButtonBusyStatus = false)
            onRefreshCompleted()
        }

        viewModelScope.launch {
            state.weighing?.weighbridgeId?.let { weighbridgeId ->
                http.getWeight(
                    weighbridgeId = weighbridgeId,
                    onEvent = { status, data, error ->
                        when (status) {
                            HttpClientCore.HttpStatus.Started -> {
                                pushProgressBar(true)
                            }

                            HttpClientCore.HttpStatus.Completed -> {
                                data?.let { weightResponse ->
                                    weightResponse.stable_weight?.let {
                                        state = state.copy(
                                            weighing = state.weighing?.copy(
                                                weightValue = it.toString()
                                            )
                                        )
                                    }
                                }
                                error?.let {
                                    pushUiNotification(error.message)
                                }
                                validateData()
                                pushProgressBar(false)
                            }

                            else -> {}
                        }
                    }
                )
            }
        }
    }

    fun setDestination(destinationEntity: DestinationEntity) {
        if (destinationEntity.weighbridgeList.isEmpty()) {
            state = state.copy(
                jobEntity = state.jobEntity?.copy(type = JobEntity.Type.Net),
                selectedDestinationEntity = destinationEntity,
                weighing = state.weighing?.copy(
                    destinationId = destinationEntity.remoteId,
                    type = Weighing.Type.Manual,
                    direction = Weighing.Direction.Net
                )
            )
        } else {
            state = state.copy(
                jobEntity = state.jobEntity?.copy(type = JobEntity.Type.Unknown),
                selectedDestinationEntity = destinationEntity,
                weighing = state.weighing?.copy(
                    destinationId = destinationEntity.remoteId,
                    type = Weighing.Type.Automatic,
                    direction = Weighing.Direction.Unknown
                )
            )
        }
        validateData()
    }

    fun setWeighbridge(weighbridge: Weighbridge?) {
        val jobType = when (weighbridge?.type) {
            WeighbridgeResponse.IN -> JobEntity.Type.In
            WeighbridgeResponse.OUT -> JobEntity.Type.Out
            WeighbridgeResponse.IN_AND_OUT -> JobEntity.Type.InAndOut
            else -> JobEntity.Type.Unknown
        }

        val weighingDirection = when (weighbridge?.type) {
            WeighbridgeResponse.IN -> Weighing.Direction.In
            WeighbridgeResponse.OUT -> Weighing.Direction.Out
            WeighbridgeResponse.IN_AND_OUT -> {
                val direction: Weighing.Direction =
                    if (state.jobEntity?.weighingIn == null && state.jobEntity?.weighingOut == null) Weighing.Direction.In
                    else if (state.jobEntity?.weighingIn != null && state.jobEntity?.weighingOut == null) Weighing.Direction.Out
                    else Weighing.Direction.Unknown
                direction
            }

            else -> Weighing.Direction.Unknown
        }

        state = state.copy(
            jobEntity = state.jobEntity?.copy(type = jobType),
            selectedWeighbridge = weighbridge,
            weighing = state.weighing?.copy(
                weighbridgeId = weighbridge?.remoteId,
                direction = weighingDirection
            )
        )
        validateData()
    }

    fun setWasteType(wasteTypeEntity: WasteTypeEntity) {
        state = state.copy(
            selectedWasteTypeEntity = wasteTypeEntity,
            weighing = state.weighing?.copy(
                wasteTypeId = wasteTypeEntity.remoteId
            )
        )
        validateData()
    }

    fun setWeightValue(value: String?) {
        state = state.copy(
            weighing = state.weighing?.copy(
                weightValue = value
            )
        )
        validateData()
    }

    private fun validateData() {
        if (state.selectedDestinationEntity == null) {
            state = state.copy(weighbridgeSectionVisibilityStatus = false)
        } else {
            if (state.selectedDestinationEntity!!.weighbridgeList.isEmpty()) {
                state = state.copy(weighbridgeSectionVisibilityStatus = false)
            } else {
                state = state.copy(weighbridgeSectionVisibilityStatus = true)
            }
        }

        if (state.selectedDestinationEntity == null || state.selectedWasteTypeEntity == null) {
            state = state.copy(weightSectionVisibilityStatus = false)
        } else {
            if (!state.selectedDestinationEntity!!.weighbridgeList.isEmpty() && state.selectedWeighbridge == null) {
                state = state.copy(weightSectionVisibilityStatus = false)
            } else {
                state = state.copy(weightSectionVisibilityStatus = true)
            }
        }

        if (state.selectedDestinationEntity == null || state.selectedWasteTypeEntity == null || state.weighing?.weightValue.isNullOrEmpty()) {
            state = state.copy(dataValidationStatus = false)
        } else {
            if (!state.selectedDestinationEntity!!.weighbridgeList.isEmpty() && state.selectedWeighbridge == null) {
                state = state.copy(dataValidationStatus = false)
            } else {
                state = state.copy(dataValidationStatus = true)
            }
        }
    }

    fun getPhoto() {
        emitEvent(Event.GET_PHOTO)
    }

    fun setPhoto(path: String) {
        state = state.copy(
            photoPath = path,
            weighing = state.weighing?.copy(docketPhoto = path)
        )
    }

    fun deletePhoto() {
        state = state.copy(
            photoPath = null,
            weighing = state.weighing?.copy(docketPhoto = null)
        )
    }

    fun pushPhotoRequiredDialog(visibilityStatus: Boolean) {
        state = state.copy(photoRequiredDialogVisibilityStatus = visibilityStatus)
    }

    fun pushConfirmationDialog(visibilityStatus: Boolean) {
        state = state.copy(confirmationDialogVisibilityStatus = visibilityStatus)
    }

    fun pushSuccessDialog(visibilityStatus: Boolean) {
        state = state.copy(successDialogVisibilityStatus = visibilityStatus)
    }

    fun pushErrorDialog(visibilityStatus: Boolean) {
        state = state.copy(errorDialogVisibilityStatus = visibilityStatus)
    }

    fun clearData() {
        state = state.copy(
            progressBarVisibilityStatus = false,
            jobEntity = null,
            weighing = null,
            //destinationEntityList = null,
            //wasteTypeEntityList = null,
            dataValidationStatus = false,
            selectedDestinationEntity = null,
            selectedWeighbridge = null,
            selectedWasteTypeEntity = null,
            weightSectionVisibilityStatus = false,
            weighbridgeSectionVisibilityStatus = false,
            photoPath = null,
            photoRequiredDialogVisibilityStatus = false,
            confirmationDialogVisibilityStatus = false,
            successDialogVisibilityStatus = false,
            errorDialogVisibilityStatus = false,
            destinationSpinnerEnableStatus = true,
            weighbridgeSpinnerEnableStatus = true,
            wasteTypeSpinnerEnableStatus = true
        )
    }

    fun getFormattedTimeSeconds(time: Long?): String? {
        time?.let {
            return repositoryLocal.getFormattedTimeSeconds(it)
        }
        return null
    }
}