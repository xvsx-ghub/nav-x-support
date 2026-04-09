package com.wiswm.nav.support.data.remote.stomp

import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.util.System
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.DelicateCoroutinesApi
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.config.HeartBeatTolerance
import org.hildan.krossbow.stomp.config.StompConfig
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(DelicateCoroutinesApi::class)
actual class StompManager actual constructor(
    private val repositoryLocal: RepositoryLocal,
    private val system: System
) {

    companion object Companion {
        const val TAG = "StompManager"
    }

    val stomp = Stomp(repositoryLocal, system)
    val stompClient = createStompClient()
    var state: Stomp.Event = Stomp.Event.UNKNOWN

    fun createStompClient(): StompClient {
        val httpClient = HttpClient(Darwin) {
            install(WebSockets)
        }

        val wsClient = KtorWebSocketClient(httpClient)
        val stompConfig = StompConfig()
        stompConfig.receiptTimeout = 1.minutes
        stompConfig.heartBeat = HeartBeat(1.minutes, 1.minutes)
        stompConfig.heartBeatTolerance = HeartBeatTolerance(30.seconds, 30.seconds)
        return StompClient(wsClient, stompConfig)
    }

    actual fun start(stompConnectionDetails: StompConnectionDetails): Boolean {
        stomp.startListening(stompClient, stompConnectionDetails)
        return true
    }

    actual fun stop(): Boolean {
        stomp.stopConnection()
        return true
    }

    actual suspend fun collectStompEvent(onEvent: (Stomp.Event) -> Unit) {
        stomp.collectStompEvent { event ->
            state = event
            onEvent(event)
        }
    }

    actual fun getLastStompEvent(): Stomp.Event?{
        return stomp.getLastStompEvent()
    }

    actual suspend fun collectStompData(onMessage: suspend (StompMessage) -> Unit) {
        stomp.collectStompData { message ->
            onMessage(message)
        }
    }
}