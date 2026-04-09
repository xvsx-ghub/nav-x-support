package com.wiswm.nav.support.data.useCase

import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.remote.http.Http
import com.wiswm.nav.support.data.remote.http.HttpClientCore.HttpStatus

class LoginUseCase(val http: Http, val repositoryLocal: RepositoryLocal) {

    enum class Event {
        UNKNOWN,
        STARTED,
        COMPLETED
    }

    suspend operator fun invoke(
        data: String, hardwareModel: String, osModel: String, applicationModel: String,
        onEvent: suspend (event: Event, errorMessage: String?) -> Unit
    ) {
        if (!isWisData(data)) return

        val tmp = data.split(":;:")
        val pin = tmp[1]
        val token = tmp[2].toInt()
        val hash = tmp[3]
        val truckId = tmp[4].toInt()
        val truckReg = tmp[5]

        getServer(
            pin = pin,
            onStarted = {
                onEvent(Event.STARTED, null)
            },
            onSuccess = {
                getAuthorization(
                    truckId = truckId,
                    truckReg = truckReg,
                    authOnly = "1",
                    userId = token.toString(),
                    hash = hash,
                    onSuccess = {
                        getAppConfig(
                            truckReg = truckReg,
                            onSuccess = {
                                getTruckReportList(
                                    onSuccess = {
                                        getCustomerList(
                                            onSuccess = {
                                                getCustomerTaskList(
                                                    onSuccess = {
                                                        getWasteTypeList(
                                                            onSuccess = {
                                                                getDestinationList(
                                                                    onSuccess = {
                                                                        getNotServicingReasonList(
                                                                            onSuccess = {
                                                                                onEvent(
                                                                                    Event.COMPLETED,
                                                                                    null
                                                                                )
                                                                            },
                                                                            onError = { errorMessage ->
                                                                                onEvent(
                                                                                    Event.COMPLETED,
                                                                                    errorMessage
                                                                                )
                                                                            }
                                                                        )
                                                                    },
                                                                    onError = { errorMessage ->
                                                                        onEvent(
                                                                            Event.COMPLETED,
                                                                            errorMessage
                                                                        )
                                                                    }
                                                                )
                                                            },
                                                            onError = { errorMessage ->
                                                                onEvent(
                                                                    Event.COMPLETED,
                                                                    errorMessage
                                                                )
                                                            }
                                                        )
                                                    },
                                                    onError = { errorMessage ->
                                                        onEvent(
                                                            Event.COMPLETED,
                                                            errorMessage
                                                        )
                                                    }
                                                )
                                            },
                                            onError = { errorMessage ->
                                                onEvent(
                                                    Event.COMPLETED,
                                                    errorMessage
                                                )
                                            }
                                        )
                                    },
                                    onError = { errorMessage ->
                                        onEvent(
                                            Event.COMPLETED,
                                            errorMessage
                                        )
                                    }
                                )
                            },
                            onError = { errorMessage ->
                                onEvent(
                                    Event.COMPLETED,
                                    errorMessage
                                )
                            }
                        )
                    },
                    onError = { errorMessage ->
                        onEvent(
                            Event.COMPLETED,
                            errorMessage
                        )
                    }
                )
            },
            onError = { errorMessage ->
                onEvent(
                    Event.COMPLETED,
                    errorMessage
                )
            }
        )
    }

    private fun isWisData(result: String) = result.startsWith("WIS:;:")

    suspend fun getServer(
        pin: String,
        onStarted: suspend () -> Unit,
        onSuccess: suspend () -> Unit,
        onError: suspend (errorMessage: String) -> Unit
    ) {
        http.getServer(
            pin = pin,
            onEvent = { status, data, error ->
                when (status) {
                    HttpStatus.Started -> {
                        onStarted()
                    }

                    HttpStatus.Completed -> {
                        data?.let { server ->
                            server.let {
                                repositoryLocal.setWisUrl(
                                    it.url.replaceFirst(
                                        "http://",
                                        "https://"
                                    ) + "/" + "WIS/rest/wma"
                                )
                                repositoryLocal.setWisName(it.name)
                            }
                            onSuccess()
                        }

                        error?.let {
                            onError(it.message ?: "")
                        }
                    }

                    else -> {}
                }
            }
        )
    }

    suspend fun getAuthorization(
        truckId: Int,
        truckReg: String,
        authOnly: String,
        userId: String,
        hash: String,
        onSuccess: suspend () -> Unit,
        onError: suspend (errorMessage: String) -> Unit
    ) {
        http.getAuthorization(
            authOnly = authOnly,
            userId = userId,
            hash = hash,
            onEvent = { status, data, error ->
                when (status) {
                    HttpStatus.Completed -> {
                        data?.let {
                            repositoryLocal.update(
                                data.mapToAuthorizationEntity(
                                    truckId = truckId,
                                    truckReg = truckReg,
                                    truckUserId = userId,
                                    truckHash = hash
                                )
                            )
                            onSuccess()
                        }
                        error?.let {
                            onError(it.message ?: "")
                        }
                    }

                    else -> {}
                }
            }
        )
    }

    suspend fun getAppConfig(
        truckReg: String,
        onSuccess: suspend () -> Unit,
        onError: suspend (errorMessage: String) -> Unit
    ) {
        http.getAppConfig(
            truckNumber = truckReg,
            onEvent = { status, data, error ->
                when (status) {
                    HttpStatus.Completed -> {
                        data?.let {
                            repositoryLocal.update(it.mapToStompEntity())
                            onSuccess()
                        }
                        error?.let {
                            onError(it.message ?: "")
                        }
                    }

                    else -> {}
                }
            }
        )
    }

    suspend fun getTruckReportList(
        onSuccess: suspend () -> Unit,
        onError: suspend (errorMessage: String) -> Unit
    ) {
        http.getTruckReportList(
            onEvent = { status, data, error ->
                when (status) {
                    HttpStatus.Completed -> {
                        data?.let {
                            repositoryLocal.clearTruckReportEntityList()
                            repositoryLocal.insertTruckReportEntityList(
                                it.TruckReportParams.map { notServicingReasonResponse ->
                                    notServicingReasonResponse.mapToTruckReportEntity(
                                        value = null,
                                        photoPath = null,
                                        checkedStatus = false
                                    )
                                })
                            onSuccess()
                        }
                        error?.let {
                            onError(it.message ?: "")
                        }
                    }

                    else -> {}
                }
            }
        )
    }

    suspend fun getCustomerList(
        onSuccess: suspend () -> Unit,
        onError: suspend (errorMessage: String) -> Unit
    ) {
        http.getCustomerList(
            onEvent = { status, data, error ->
                when (status) {
                    HttpStatus.Completed -> {
                        data?.let { customerResponseList ->
                            customerResponseList.customers?.let {
                                repositoryLocal.clearCustomerTable()
                                repositoryLocal.insertCustomerEntityList(it.map { customerResponse -> customerResponse.mapToCustomerEntity() })
                            }
                        }
                        onSuccess()
                    }

                    else -> {}
                }
            }
        )
    }

    suspend fun getCustomerTaskList(
        onSuccess: suspend () -> Unit,
        onError: suspend (errorMessage: String) -> Unit
    ) {
        http.getCustomerTaskList(
            onEvent = { status, data, error ->
                when (status) {
                    HttpStatus.Completed -> {
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
                        onSuccess()
                    }

                    else -> {}
                }
            }
        )
    }

    suspend fun getWasteTypeList(
        onSuccess: suspend () -> Unit,
        onError: suspend (errorMessage: String) -> Unit
    ) {
        http.getWasteTypeList(
            onEvent = { status, data, error ->
                when (status) {
                    HttpStatus.Completed -> {
                        data?.let { nnWasteTypeListResponse ->
                            nnWasteTypeListResponse.WasteTypes?.let { nnWasteTypes ->
                                repositoryLocal.clearWasteTypeEntityList()
                                repositoryLocal.insertWasteTypeEntityList(
                                    nnWasteTypes.map { wasteType ->
                                        wasteType.mapToWasteTypeEntity()
                                    }
                                )
                            }
                            onSuccess()
                        }
                        error?.let {
                            onError(it.message ?: "")
                        }
                    }

                    else -> {}
                }
            }
        )
    }

    suspend fun getDestinationList(
        onSuccess: suspend () -> Unit,
        onError: suspend (errorMessage: String) -> Unit
    ) {
        http.getDestinationList(
            onEvent = { status, data, error ->
                when (status) {
                    HttpStatus.Completed -> {
                        data?.let { nnDestinationListResponse ->
                            nnDestinationListResponse.destinationDescriptions?.let { nnDestinationResponse ->
                                repositoryLocal.clearDestinationEntityList()
                                repositoryLocal.insertDestinationEntityList(
                                    nnDestinationResponse.map { destinationResponse ->
                                        destinationResponse.mapToDestinationEntity()
                                    }
                                )
                            }

                            onSuccess()
                        }
                        error?.let {
                            onError(it.message ?: "")
                        }
                    }

                    else -> {}
                }
            }
        )
    }

    suspend fun getNotServicingReasonList(
        onSuccess: suspend () -> Unit,
        onError: suspend (errorMessage: String) -> Unit
    ) {
        http.getNotServicingReasonList(
            onEvent = { status, data, error ->
                when (status) {
                    HttpStatus.Completed -> {
                        data?.let {
                            repositoryLocal.clearNotServicingReasonEntityList()
                            repositoryLocal.insertNotServicingReasonEntityList(
                                it.BinNotServicingReasons.map { notServicingReasonResponse ->
                                    notServicingReasonResponse.mapToNotServicingReasonEntity()
                                })
                            onSuccess()
                        }
                        error?.let {
                            onError(it.message ?: "")
                        }
                    }

                    else -> {}
                }
            }
        )
    }
}