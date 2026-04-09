package com.wiswm.nav.support.data.remote.http

import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.local.dataBase.entity.RequestEntity
import com.wiswm.nav.support.data.local.dataBase.entity.TaskEntity
import com.wiswm.nav.support.util.Logger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

open class HttpClientCore(protected val repositoryLocal: RepositoryLocal) {
    companion object {
        const val TAG = "HttpClientCore"
        const val SESSION_EXPIRED_STATUS_CODE = 401
    }

    enum class HttpStatus {
        Started,
        Completed,
        Busy
    }

    enum class RequestType {
        /*
        Invokes error by sending fails.
        Not stores data for resending attempts.
        */
        Online,

        /*
        Ignores error by sending fails.
        Stores data for resending attempts.
        */
        Offline,

        /*
        Invokes error by sending fails.
        Stores data for resending attempts.
        */
        Mixed
    }

    enum class RequestMethod {
        Post,
        Get
    }

    @Serializable
    data class Recyn<T>(
        val status: Boolean,
        val name: String,
        val url: String,
        val statusCode: Int,
        val data: T
    )

    @Serializable
    data class Wis<T>(
        val status: Boolean,
        val message: String = "",
        val error_code: String = "",
        val statusCode: Int = 0,
        val data: T? = null
    )

    data class Request(
        val url: String,
        val paramHashMap: HashMap<String, String>,
        val imageFileHashMap: HashMap<String, String>? = null,
        val typeValue: String,
        val methodValue : String
    ) {
        fun mapToRequestEntity() =
            RequestEntity(
                url = url,
                paramHashMap = paramHashMap,
                imageFileHashMap = imageFileHashMap,
                typeValue = typeValue,
                methodValue = methodValue
            )

        fun mapToFormData(imageDataList: List<ImageData>?) = formData {
            imageDataList?.let {
                imageDataList.forEach { imageData ->
                    append(
                        imageData.name,
                        imageData.byteArray,
                        Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"${imageData.name}\"; filename=\"${imageData.fileName}\""
                            )
                        }
                    )
                }
            }

            paramHashMap.forEach { paramHashMap ->
                append(paramHashMap.key, paramHashMap.value)
            }
        }

        fun updateSessionKey(sessionKey: String){
            paramHashMap[SESSION_KEY_PARAM_HASHMAP_KEY] = sessionKey
        }

        companion object{
            const val SESSION_KEY_PARAM_HASHMAP_KEY = "sessionKey"
        }
    }

    data class ImageData(
        val name: String,
        val fileName: String,
        val byteArray: ByteArray,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as ImageData
            if (name != other.name) return false
            if (!byteArray.contentEquals(other.byteArray)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + byteArray.contentHashCode()
            return result
        }
    }

    @Serializable
    data class UnknownData(
        val unknownValue: JsonElement? = null
    )

    @Serializable
    data class ListData<T>(
        val listData: List<T>? = null
    )

    suspend fun get(imageFileHashMap: HashMap<String, String>?): List<ImageData>? {
        var imageDataList: MutableList<ImageData>? = null
        imageFileHashMap?.let { nnImageFileHashMap ->
            imageDataList = mutableListOf()
            nnImageFileHashMap.forEach { imageFile ->
                val byteArray = repositoryLocal.loadBytesFromImagePath(imageFile.value)
                byteArray?.let {
                    imageDataList.add(ImageData(name = imageFile.key, fileName = repositoryLocal.getFormattedCurrentTimeSeconds() + ".jpg", byteArray = it))
                }
            }
        }
        return imageDataList
    }

    protected val recynRootUrl = "https://recyn.com/rest/rest"
    protected val client: HttpClient = HttpClientFactory().create()
    protected var isLoading = false

    protected suspend inline fun <reified T> get(
        request: Request,
        crossinline onEvent: suspend (status: HttpStatus, data: T?, error: Exception?) -> Unit
    ) {
        var response: T? = null
        var exception: Exception? = null
        if (isLoading) {
            Logger.d(TAG, "Abort request to ${request.url}")
            onEvent(HttpStatus.Busy, null, null)
            return
        }
        val fullUrl = buildUrlWithFormData(request)
        isLoading = true
        onEvent(HttpStatus.Started, null, null)
        try {
            val httpResponse = withContext(Dispatchers.IO) {
                client.get(fullUrl)
            }
            val bodyText = httpResponse.bodyAsText()
            Logger.d(TAG, "Response from $fullUrl : $bodyText")
            val json = Json { ignoreUnknownKeys = true }
            response = json.decodeFromString<T>(bodyText)
        } catch (e: Exception) {
            Logger.d(TAG, "Exception from $fullUrl : ${e.message}")
            when (RequestType.valueOf(request.typeValue)) {
                RequestType.Online -> {
                    exception = e
                }

                RequestType.Offline -> {
                    exception = null
                    repositoryLocal.insert(
                        request.mapToRequestEntity()
                    )
                    Logger.d(TAG, "Request saved to DB. Url: ${request.url}")
                }

                RequestType.Mixed -> {
                    exception = e
                    repositoryLocal.insert(
                        request.mapToRequestEntity()
                    )
                    Logger.d(TAG, "Request saved to DB. Url: ${request.url}")
                }
            }
        } finally {
            isLoading = false
            onEvent(HttpStatus.Completed, response, exception)
        }
    }

    protected suspend inline fun <reified T> post(
        request: Request,
        crossinline onEvent: suspend (status: HttpStatus, data: T?, error: Exception?) -> Unit
    ) {
        var response: T? = null
        var exception: Exception? = null

        if (isLoading) {
            Logger.d(TAG, "Abort request to ${request.url}")
            onEvent(HttpStatus.Busy, null, null)
            return
        }

        isLoading = true
        onEvent(HttpStatus.Started, null, null)
        try {
            val httpResponse = withContext(Dispatchers.IO) {
                client.submitFormWithBinaryData(
                    url = request.url,
                    formData = request.mapToFormData(get(request.imageFileHashMap))
                )
            }
            val bodyText = httpResponse.bodyAsText()
            Logger.d(TAG, "Response from ${request.url} : $bodyText")
            val json = Json { ignoreUnknownKeys = true }
            response = json.decodeFromString<T>(bodyText)
        } catch (e: Exception) {
            Logger.d(TAG, "Exception from ${request.url} : ${e.message}")
            when (RequestType.valueOf(request.typeValue)) {
                RequestType.Online -> {
                    exception = e
                }

                RequestType.Offline -> {
                    exception = null
                    repositoryLocal.insert(
                        request.mapToRequestEntity()
                    )
                    Logger.d(TAG, "Request saved to DB. Url: ${request.url}")
                }

                RequestType.Mixed -> {
                    exception = e
                    repositoryLocal.insert(
                        request.mapToRequestEntity()
                    )
                    Logger.d(TAG, "Request saved to DB. Url: ${request.url}")
                }
            }
        } finally {
            isLoading = false
            onEvent(HttpStatus.Completed, response, exception)
        }
    }

    protected fun buildUrlWithFormData(
        request: Request
    ): String {
        return URLBuilder(request.url).apply {
            request.paramHashMap.forEach { param ->
                parameters.append(param.key, param.value)
            }
        }.buildString()
    }

    suspend fun synchronizeOfflineData() {
        val requestEntity = repositoryLocal.getLastRequestEntity()
        requestEntity?.let { nnRequestEntity ->

            val request = nnRequestEntity.mapToRequest()
            request.updateSessionKey(repositoryLocal.getSessionKey())

            when (RequestMethod.valueOf(request.methodValue)) {
                RequestMethod.Get -> {
                    Logger.d(TAG, "Offline data synchronization. Get. Url: ${requestEntity.url}")
                    get<Wis<UnknownData>>(
                        request = request,
                        onEvent = { status, data, error ->
                            when (status) {
                                HttpStatus.Completed -> {
                                    if (data?.status ?: false) {
                                        repositoryLocal.delete(nnRequestEntity)
                                        Logger.d(
                                            TAG,
                                            "Request deleted from DB. Url: ${requestEntity.url}"
                                        )
                                        synchronizeOfflineData()
                                    }
                                }

                                else -> {}
                            }
                        }
                    )
                }

                RequestMethod.Post -> {
                    Logger.d(TAG, "Offline data synchronization. Psst. Url: ${requestEntity.url}")
                    post<Wis<UnknownData>>(
                        request = request,
                        onEvent = { status, data, error ->
                            when (status) {
                                HttpStatus.Completed -> {
                                    if (data?.status ?: false) {
                                        when {
                                            request.url.contains("TruckRouter/SetLift") -> {
                                                request.imageFileHashMap?.let { nnImageFileHashMap ->
                                                    nnImageFileHashMap.forEach { imageFileHash ->
                                                        val taskEntity =
                                                            repositoryLocal.getTaskEntityByImageFilePath(
                                                                imageFileHash.value
                                                            )
                                                        taskEntity?.let { nnTaskEntity ->
                                                            //system.deleteFile(taskEntity.filePath)
                                                            nnTaskEntity.status =
                                                                TaskEntity.DONE_STATUS
                                                            repositoryLocal.update(nnTaskEntity)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        repositoryLocal.delete(nnRequestEntity)
                                        Logger.d(
                                            TAG,
                                            "Request deleted from DB. Url: ${requestEntity.url}"
                                        )
                                        synchronizeOfflineData()
                                    }
                                }

                                else -> {}
                            }
                        }
                    )
                }
            }
        }
    }

    protected suspend inline fun <reified T> resend(
        sessionKey: String,
        request: Request,
        noinline onEvent: suspend (status: HttpStatus, data: T?, error: Exception?) -> Unit
    ) {
        Logger.d(Http.Companion.TAG, "Old paramHashMap: ${request.paramHashMap}")
        request.updateSessionKey(sessionKey)
        Logger.d(Http.Companion.TAG, "New paramHashMap: ${request.paramHashMap}")

        when (RequestMethod.valueOf(request.methodValue)) {
            RequestMethod.Get -> {
                get<Wis<T>>(
                    request = request,
                    onEvent = { status, data, error ->
                        if (data == null) {
                            onEvent(status, null, error)
                        } else {
                            if (data.status) {
                                onEvent(status, data.data, null)
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
                )
            }

            RequestMethod.Post -> {
                post<Wis<T>>(
                    request = request,
                    onEvent = { status, data, error ->
                        if (data == null) {
                            onEvent(status, null, error)
                        } else {
                            if (data.status) {
                                onEvent(status, data.data, null)
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
                )
            }
        }
    }
// remember to close client when appropriate (or reuse a single client per app lifecycle)
}