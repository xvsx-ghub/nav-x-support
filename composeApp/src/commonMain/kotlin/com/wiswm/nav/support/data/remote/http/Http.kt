package com.wiswm.nav.support.data.remote.http

import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.dataBase.entity.TruckReportEntity
import com.wiswm.nav.support.data.remote.http.response.AppConfigResponse
import com.wiswm.nav.support.data.remote.http.response.AuthorizationResponse
import com.wiswm.nav.support.data.remote.http.response.CustomerListResponse
import com.wiswm.nav.support.data.remote.http.response.CustomerTaskListResponse
import com.wiswm.nav.support.data.remote.http.response.DestinationListResponse
import com.wiswm.nav.support.data.remote.http.response.NotServicingReasonListResponse
import com.wiswm.nav.support.data.remote.http.response.PendingWeighingListResponse
import com.wiswm.nav.support.data.remote.http.response.RouteResponse
import com.wiswm.nav.support.data.remote.http.response.ServerResponse
import com.wiswm.nav.support.data.remote.http.response.TruckReportListResponse
import com.wiswm.nav.support.data.remote.http.response.WasteTypeListResponse
import com.wiswm.nav.support.data.remote.http.response.WeighingResponse
import com.wiswm.nav.support.data.remote.http.response.WeightResponse
import com.wiswm.nav.support.util.Logger

class Http(repositoryLocal: RepositoryLocal) : HttpClientCore(repositoryLocal) {
    companion object {
        const val TAG = "Http"
    }

    suspend fun getServer(
        pin: String,
        onEvent: suspend (status: HttpStatus, data: ServerResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "getclientbypin"
        val request = Request(
            url = "$recynRootUrl/$requestName",
            paramHashMap = hashMapOf(
                "pin" to pin,
            ),
            typeValue = RequestType.Online.name,
            methodValue = RequestMethod.Get.name
        )
        get<Recyn<ServerResponse>>(
            request = request,
            onEvent = { status, data, error ->
                onEvent(status, data?.data, error)
            }
        )
    }

    suspend fun getAuthorization(
        authOnly: String,
        userId: String,
        hash: String,
        onEvent: suspend (status: HttpStatus, data: AuthorizationResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "auth/login"

        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "AuthOnly" to authOnly,
                "UserId" to userId,
                "Hash" to hash,
                "HardwareModel" to repositoryLocal.getDeviceId(),
                "OsModel" to repositoryLocal.getOsVersion(),
                "ApplicationModel" to repositoryLocal.getOsVersion()
            ),
            typeValue = RequestType.Online.name,
            methodValue = RequestMethod.Get.name
        )
        get<Wis<AuthorizationResponse>>(
            request = request,
            onEvent = { status, data, error ->
                data?.data?.let {
                    repositoryLocal.setSessionKey(it.sessionKey)
                    repositoryLocal.setRosterId(it.RosterId.toString())
                }
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun getAppConfig(
        truckNumber: String,
        onEvent: suspend (status: HttpStatus, data: AppConfigResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/GetAppConfig"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey(),
                "truckNumber" to truckNumber
            ),
            typeValue = RequestType.Online.name,
            methodValue = RequestMethod.Get.name
        )
        get<Wis<AppConfigResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun setPhoto(
        customerId: String,
        transactionId: String,
        receivedDeviceId: String,
        timeStamp: String,
        notServicingReasonId: String,
        imagePath: String,
        taskIds: String,
        onEvent: suspend (status: HttpStatus, data: UnknownData?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/SetLift"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey(),
                "device_id" to repositoryLocal.getDeviceId(),
                "version" to repositoryLocal.getAppVersion(),
                "CustomerID" to customerId,
                "weight_transaction_number" to transactionId,
                "weight_device_identification" to receivedDeviceId,
                "CreatedAt" to timeStamp,
                "NotServicingReason" to notServicingReasonId,
                "task_ids" to taskIds
            ),
            imageFileHashMap = hashMapOf("ProblemImage" to imagePath),
            typeValue = RequestType.Mixed.name,
            methodValue = RequestMethod.Post.name
        )
        post<Wis<UnknownData?>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun setSignature(
        customerId: String,
        transactionId: String,
        receivedDeviceId: String,
        notServicingReasonId: String,
        imagePath: String,
        taskIds: String,
        onEvent: suspend (status: HttpStatus, data: UnknownData?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/SetLift"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey(),
                "device_id" to repositoryLocal.getDeviceId(),
                "version" to repositoryLocal.getAppVersion(),
                "CustomerID" to customerId,
                "weight_transaction_number" to transactionId,
                "weight_device_identification" to receivedDeviceId,
                "CreatedAt" to repositoryLocal.getFormattedCurrentTimeSeconds(),
                "NotServicingReason" to notServicingReasonId,
                "task_ids" to taskIds
            ),
            imageFileHashMap = hashMapOf("SignatureImage" to imagePath),
            typeValue = RequestType.Mixed.name,
            methodValue = RequestMethod.Post.name
        )
        post<Wis<UnknownData?>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun getCustomerList(
        onEvent: suspend (status: HttpStatus, data: CustomerListResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/GetCustomers"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey()
            ),
            typeValue = RequestType.Online.name,
            methodValue = RequestMethod.Get.name
        )
        get<Wis<CustomerListResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun getNotServicingReasonList(
        onEvent: suspend (status: HttpStatus, data: NotServicingReasonListResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/GetNotServicingReason"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey()
            ),
            typeValue = RequestType.Online.name,
            methodValue = RequestMethod.Get.name
        )
        get<Wis<NotServicingReasonListResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun getTruckReportList(
        onEvent: suspend (status: HttpStatus, data: TruckReportListResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/GetTruckReportParams"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey()
            ),
            typeValue = RequestType.Online.name,
            methodValue = RequestMethod.Get.name
        )
        get<Wis<TruckReportListResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun setTruckReport(
        truckNumber: String,
        driverId: String,
        truckReportEntityList: List<TruckReportEntity>,
        onEvent: suspend (status: HttpStatus, data: UnknownData?, error: Exception?) -> Unit
    ) {
        val params = TruckReportEntity.convertTruckReportEntityListToString(truckReportEntityList)

        val imageFileHashMap = hashMapOf<String, String>()
        truckReportEntityList.forEach { truckReportEntity ->
            truckReportEntity.photoPath?.let {
                imageFileHashMap.put("Image[${truckReportEntity.key}]", truckReportEntity.photoPath)
            }
        }

        val requestName = "TruckRouter/SetTruckReport"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey(),
                "CreatedAt" to repositoryLocal.getFormattedCurrentTimeSeconds(),
                "truckNumber" to truckNumber,
                "DriverId" to driverId,
                "Params" to params,
            ),
            imageFileHashMap = imageFileHashMap,
            typeValue = RequestType.Mixed.name,
            methodValue = RequestMethod.Post.name
        )
        post<Wis<UnknownData>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun getCustomerTaskList(
        onEvent: suspend (status: HttpStatus, data: CustomerTaskListResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/GetTasks"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey()
            ),
            typeValue = RequestType.Online.name,
            methodValue = RequestMethod.Get.name
        )
        get<Wis<CustomerTaskListResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun getWasteTypeList(
        onEvent: suspend (status: HttpStatus, data: WasteTypeListResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/GetWasteTypes"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey()
            ),
            typeValue = RequestType.Online.name,
            methodValue = RequestMethod.Get.name
        )
        get<Wis<WasteTypeListResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun getDestinationList(
        onEvent: suspend (status: HttpStatus, data: DestinationListResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/GetDestinationDescriptions"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey()
            ),
            typeValue = RequestType.Online.name,
            methodValue = RequestMethod.Get.name
        )
        get<Wis<DestinationListResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun setWeightIn(
        wasteTypeId: String,
        destinationId: String,
        weighbridgeId: String,
        weight: String,
        timestamp: String,
        driverId: String,
        routeId: String,
        truckId: String,
        onEvent: suspend (status: HttpStatus, data: WeighingResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/AddWeighbridgeWeightIn"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey(),
                "CreatedAt" to repositoryLocal.getFormattedCurrentTimeSeconds(),
                "RosterId" to repositoryLocal.getRosterId(),
                "WasteTypeId" to wasteTypeId,
                "DestinationId" to destinationId,
                "WeighbridgeIdIn" to weighbridgeId,
                "WeightIn" to weight,
                "TimestampIn" to timestamp,
                "DriverId" to driverId,
                "RouteId" to routeId,
                "TruckId" to truckId
            ),
            typeValue = RequestType.Mixed.name,
            methodValue = RequestMethod.Post.name
        )
        post<Wis<WeighingResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun setWeightOut(
        weighingId: String,
        weighbridgeId: String,
        timestamp: String,
        weight: String,
        onEvent: suspend (status: HttpStatus, data: WeighingResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/AddWeighbridgeWeightOut"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey(),
                "CreatedAt" to repositoryLocal.getFormattedCurrentTimeSeconds(),
                "RosterId" to repositoryLocal.getRosterId(),
                "WeighingId" to weighingId,
                "WeighbridgeIdOut" to weighbridgeId,
                "TimestampOut" to timestamp,
                "WeightOut" to weight
            ),
            typeValue = RequestType.Mixed.name,
            methodValue = RequestMethod.Post.name
        )
        post<Wis<WeighingResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun setWeightNet(
        wasteTypeId: String,
        destinationId: String,
        weightIn: String,
        timestampIn: String,
        weightOut: String,
        driverId: String,
        routeId: String,
        truckId: String,
        onEvent: suspend (status: HttpStatus, data: WeighingResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/AddExternalWeighing"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey(),
                "CreatedAt" to repositoryLocal.getFormattedCurrentTimeSeconds(),
                "RosterId" to repositoryLocal.getRosterId(),
                "WasteTypeId" to wasteTypeId,
                "DestinationId" to destinationId,
                "WeightIn" to weightIn,
                "TimestampIn" to timestampIn,
                "WeightOut" to weightOut,
                "DriverId" to driverId,
                "RouteId" to routeId,
                "TruckId" to truckId
            ),
            typeValue = RequestType.Mixed.name,
            methodValue = RequestMethod.Post.name
        )
        post<Wis<WeighingResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun getWeight(
        weighbridgeId: String,
        onEvent: suspend (status: HttpStatus, data: WeightResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/GetWeighbridgeWeight"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey(),
                "WeighbridgeId" to weighbridgeId
            ),
            typeValue = RequestType.Mixed.name,
            methodValue = RequestMethod.Post.name
        )
        post<Wis<WeightResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun getPendingWeighingList(
        driverId: String,
        truckId: String,
        onEvent: suspend (status: HttpStatus, data: PendingWeighingListResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/GetPendingWeighings"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey(),
                "DriverId" to driverId, // user id
                "TruckId" to truckId // truck reg number
            ),
            typeValue = RequestType.Mixed.name,
            methodValue = RequestMethod.Post.name
        )
        post<Wis<PendingWeighingListResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    suspend fun getCurrentRoute(
        onEvent: suspend (status: HttpStatus, data: RouteResponse?, error: Exception?) -> Unit
    ) {
        val requestName = "TruckRouter/GetRoute"
        val request = Request(
            url = repositoryLocal.getWisUrl() + "/$requestName",
            paramHashMap = hashMapOf(
                "sessionKey" to repositoryLocal.getSessionKey(),
            ),
            typeValue = RequestType.Mixed.name,
            methodValue = RequestMethod.Post.name
        )
        post<Wis<RouteResponse>>(
            request = request,
            onEvent = { status, data, error ->
                handleResponse(
                    request = request,
                    status = status,
                    data = data,
                    error = error,
                    onEvent = onEvent
                )
            }
        )
    }

    private suspend inline fun <reified T> handleResponse(
        request: Request,
        status: HttpStatus,
        data: Wis<T>?,
        error: Exception?,
        noinline onEvent: suspend (status: HttpStatus, data: T?, error: Exception?) -> Unit
    ) {
        if (data == null) {
            onEvent(status, null, error)
        } else {
            if (data.status) {
                onEvent(status, data.data, null)
            } else {
                if (data.statusCode == SESSION_EXPIRED_STATUS_CODE) {
                    val authorizationEntity = repositoryLocal.getAuthorizationEntity()
                    if (authorizationEntity == null) {
                        onEvent(
                            status, null, Exception(
                                "Server error. " +
                                        "status: ${data.status}, " +
                                        "message: ${data.message}, " +
                                        "error_code: ${data.error_code}" +
                                        "statusCode: ${data.statusCode}" +
                                        "data: ${data.data}"
                            )
                        )
                        Logger.d(TAG, "Relogin failed. Auth initial data is empty")
                        return
                    }
                    getAuthorization(
                        authOnly = "1",
                        userId = authorizationEntity.truckUserId,
                        hash = authorizationEntity.truckHash,
                        onEvent = { status, data, error ->
                            Logger.d(
                                TAG,
                                "Relogin: stauts = $status, data = $data, error = ${error}"
                            )

                            if (error != null) {
                                Logger.d(TAG, "Relogin failed.")
                            } else {
                                data?.let {
                                    resend(it.sessionKey, request, onEvent)
                                }
                            }
                        }
                    )
                } else {
                    onEvent(
                        status, null, Exception(
                            "Server error. " +
                                    "status: ${data.status}, " +
                                    "message: ${data.message}, " +
                                    "error_code: ${data.error_code}" +
                                    "statusCode: ${data.statusCode}" +
                                    "data: ${data.data}"
                        )
                    )
                }
            }
        }
    }
    // remember to close client when appropriate (or reuse a single client per app lifecycle)
}