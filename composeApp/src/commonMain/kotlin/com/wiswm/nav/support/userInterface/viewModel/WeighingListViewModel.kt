package com.wiswm.nav.support.userInterface.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.dataBase.entity.JobEntity
import com.wiswm.nav.support.data.remote.http.Http
import com.wiswm.nav.support.data.remote.http.HttpClientCore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class WeighingListViewModel(
    private val repositoryLocal: RepositoryLocal,
    private val http: Http
) : ViewModel() {
    companion object {
        const val TAG = "WeighingListViewModel"
    }

    enum class Event {
        UNKNOWN,
        SHOW_WEIGHING_DETAILS
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
        val jobEntityList: List<JobEntity>?,
        val selectedJobEntity: JobEntity?
    )

    var state by mutableStateOf(
        State(
            uiNotificationMessage = null,
            progressBarVisibilityStatus = false,
            jobEntityList = null,
            selectedJobEntity = null
        )
    )
        private set

    init {
        serveJobData()
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

    private fun serveJobData() {
        viewModelScope.launch {
            var jobEntityList: List<JobEntity>? = repositoryLocal.getJobEntityList()
            state =
                state.copy(jobEntityList = jobEntityList?.filter { jobEntity -> !jobEntity.completedStatus })

            repositoryLocal.getJobEntityListAsFlow().collect {
                jobEntityList = it
                state =
                    state.copy(jobEntityList = jobEntityList?.filter { jobEntity -> !jobEntity.completedStatus })
            }
        }
    }

    fun getRemoteWeighingList() {
        viewModelScope.launch {
            val authorizationEntity = repositoryLocal.getAuthorizationEntity()
            http.getPendingWeighingList(
                driverId = authorizationEntity?.userId ?: "",
                truckId = authorizationEntity?.truckReg ?: "",
                onEvent = { status, data, error ->
                    when (status) {
                        HttpClientCore.HttpStatus.Started -> {
                            pushProgressBar(true)
                        }

                        HttpClientCore.HttpStatus.Completed -> {
                            data?.let { pendingWeighingListResponse ->
                                pendingWeighingListResponse.Weighings?.let { weighingResponseList ->
                                    val remoteJobEntityList =
                                        weighingResponseList.map { weighingResponse ->
                                            weighingResponse.mapToJobEntity()
                                        }
                                    val localJobEntityList = repositoryLocal.getJobEntityList()

                                    remoteJobEntityList.forEach { remoteJobEntity ->
                                        if(!checkJobForExistingByWeightIn(remoteJobEntity, localJobEntityList)){
                                            repositoryLocal.insertJobEntity(remoteJobEntity)
                                        }
                                    }
                                }
                            }
                            error?.let {
                                pushUiNotification(error.message)
                            }
                            pushProgressBar(false)
                        }

                        else -> {}
                    }
                }
            )
        }
    }

    fun checkJobForExistingByWeightIn(jobEntity: JobEntity, jobEntityList: List<JobEntity>?): Boolean{
        var existingResult = false
        jobEntityList?.forEach {
            if(jobEntity.weighingIn?.remoteId == it.weighingIn?.remoteId){
                existingResult = true
                return existingResult
            }
        }
        return existingResult
    }

    fun saveJob(jobEntity: JobEntity, onSaved: ((jobEntity: JobEntity) -> Unit)? = null) {
        viewModelScope.launch {
            val newJobEntity =
                jobEntity.copy(timestamp = repositoryLocal.getCurrentTimeSeconds().toString())
            val id = repositoryLocal.insertJobEntity(newJobEntity)
            onSaved?.let {
                it(newJobEntity.copy(id = id))
            }
        }
    }

    fun setSelectedJobEntity(jobEntity: JobEntity) {
        state = state.copy(selectedJobEntity = jobEntity)
    }

    fun showWeighingDetails() {
        emitEvent(Event.SHOW_WEIGHING_DETAILS)
    }

    fun getFormattedTimeSeconds(time: Long?): String?{
        time?.let {
            return repositoryLocal.getFormattedTimeSeconds(it)
        }
        return null
    }
}