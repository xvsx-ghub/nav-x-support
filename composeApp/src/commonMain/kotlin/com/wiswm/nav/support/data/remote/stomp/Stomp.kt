package com.wiswm.nav.support.data.remote.stomp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wiswm.nav.support.util.Logger
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.data.remote.stomp.subscriber.RouteSubscriber
import com.wiswm.nav.support.data.remote.stomp.subscriber.TaskSubscriber
import com.wiswm.nav.support.util.System
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText

class Stomp(
    private val repositoryLocal: RepositoryLocal,
    private val system: System
) {
    companion object {
        const val TAG = "Stomp"
        private const val RECONNECT_DELAY_MS = 1_000L
    }

    data class State(
        val lastEvent: Event,
    )

    var state by mutableStateOf(State(Event.UNKNOWN))
        private set

    private val _stompDataFlow = MutableSharedFlow<StompMessage>()
    val stompDataFlow: Flow<StompMessage> = _stompDataFlow

    suspend fun emitStompData(message: StompMessage) {
        _stompDataFlow.emit(message)
    }

    suspend fun collectStompData(onMessage: suspend (StompMessage) -> Unit) {
        stompDataFlow.collect { message ->
            onMessage(message)
        }
    }

    private val _stompEventFlow = MutableSharedFlow<Event>()
    val stompEventFlow: Flow<Event> = _stompEventFlow

    suspend fun emitStompEvent(event: Event) {
        state = state.copy(lastEvent = event)
        _stompEventFlow.emit(event)
    }

    suspend fun collectStompEvent(onEvent: suspend (Event) -> Unit) {
        stompEventFlow.collect { event ->
            onEvent(event)
        }
    }

    fun getLastStompEvent(): Event? {
        return state.lastEvent
    }

    enum class Event {
        UNKNOWN,
        CONNECTED,
        DISCONNECTED
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var shouldReconnect = false
    private var connectionJob: Job? = null
    private var stompSession: StompSession? = null

    fun startListening(
        stompClient: StompClient,
        stompConnectionDetails: StompConnectionDetails
    ) {
        Logger.d(TAG, "connecting...")
        shouldReconnect = true
        if (connectionJob?.isActive == true) {
            Logger.d(TAG, "new connection ignored: already running")
            return
        }

        connectionJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            while (isActive && shouldReconnect) {
                try {
                    Logger.d(TAG, "connected")
                    val session = stompClient.connect(
                        stompConnectionDetails.host,
                        stompConnectionDetails.login,
                        stompConnectionDetails.password,
                        stompConnectionDetails.vhost
                    )
                    stompSession = session
                    emitStompEvent(Event.CONNECTED)
                    subscribe(session)
                    Logger.d(TAG, "connection lost")
                } catch (e: Exception) {
                    Logger.d(TAG, "connection error: " + e.message)
                } finally {
                    stompSession = null
                    emitStompEvent(Event.DISCONNECTED)
                }

                if (shouldReconnect && isActive) {
                    delay(RECONNECT_DELAY_MS)
                }
            }
        }
    }

    private suspend fun subscribe(session: StompSession) = coroutineScope {
        val truckId = repositoryLocal.getAuthorizationEntity()?.truckId?.toString()
        val deviceId = system.getDeviceInfo().id
        if (truckId.isNullOrBlank() || deviceId.isBlank()) {
            throw IllegalStateException("Missing subscription identifiers")
        }

        val taskCollector = launch {
            val taskSessionSubscriber = session.subscribe(
                TaskSubscriber(
                    truckId,
                    deviceId
                ).headers
            )
            taskSessionSubscriber.collect { message ->
                Logger.d(
                    TAG,
                    "Stomp: subscription = TaskSubscriber, message = $message"
                )

                message.headers.ack?.let { session.ack(it) }
                emitStompData(StompMessage(TaskSubscriber.TAG, message.bodyAsText))
            }
        }

        val routeCollector = launch {
            val routeSessionSubscriber = session.subscribe(
                RouteSubscriber(
                    truckId,
                    deviceId
                ).headers
            )
            routeSessionSubscriber.collect { message ->
                Logger.d(
                    TAG,
                    "Stomp: subscription = RouteSubscriber, message = $message"
                )

                message.headers.ack?.let { session.ack(it) }
                emitStompData(StompMessage(RouteSubscriber.TAG, message.bodyAsText))
            }
        }

        joinAll(taskCollector, routeCollector)
    }

    fun stopConnection() {
        Logger.d(TAG, "stopConnection()")
        shouldReconnect = false

        scope.launch {
            connectionJob?.cancelAndJoin()
            connectionJob = null
            stompSession?.let { disconnect(it) }
            stompSession = null
            emitStompEvent(Event.DISCONNECTED)
        }
    }

    private suspend fun disconnect(stompSession: StompSession): Boolean {
        try {
            emitStompEvent(Event.DISCONNECTED)
            stompSession.disconnect()
            return true
        } catch (e: Exception) {
            /*
            state.connectState = false
            Logger.e(TAG, e.message ?: "disconnect() Exception")
            Logger.d(TAG, "disconnect() error")
            return false
            */
            //Impossible to avoid the exception. Probably library issue. The exception is throwing even by simple reconnection.
            //Fix:
            return true
            //
        }
    }

    private suspend fun send(
        stompSession: StompSession?,
        path: String,
        message: String
    ): Boolean {
        if (stompSession == null) {
            Logger.d(
                TAG,
                "send() error. Stomp session is not created. Establish connection first!"
            )
            return false
        }
        stompSession.sendText(path, message)
        return true
    }
}