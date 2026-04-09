package com.wiswm.nav.support.data.remote.stomp

import android.content.Context
import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.util.Logger
import com.wiswm.nav.support.util.System
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.websocket.WebSockets
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.config.HeartBeat
import org.hildan.krossbow.stomp.config.StompConfig
import org.hildan.krossbow.websocket.ktor.KtorWebSocketClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.time.Duration.Companion.minutes

lateinit var stompContext: Context

actual class StompManager actual constructor(
    private val repositoryLocal: RepositoryLocal,
    private val system: System
) {
    companion object Companion {
        const val TAG = "StompManager"
    }
    val stomp = Stomp(repositoryLocal, system)
    val stompClient = createStompClient()

    fun createStompClient(): StompClient {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }

        val httpClient = HttpClient(OkHttp) {
            engine {
                config {
                    sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                }
            }
            install(WebSockets)
        }

        val wsClient = KtorWebSocketClient(httpClient)
        val stompConfig = StompConfig()
        stompConfig.heartBeat = HeartBeat(1.minutes, 1.minutes)
        return StompClient(wsClient, stompConfig)
    }

    actual fun start(stompConnectionDetails: StompConnectionDetails): Boolean {
        if (!::stompContext.isInitialized) {
            Logger.e(TAG, "lateinit var stompContext: Context is not initialized")
            return false
        }

        StompLifecycleService.start(
            stompContext,
            stomp,
            stompClient,
            stompConnectionDetails
        ) { exception ->
            Logger.d(TAG, "appForegroundModeEvent() Error: " + exception.message)
        }
        return true
    }

    actual fun stop(): Boolean {
        if (!::stompContext.isInitialized) {
            Logger.e(TAG, "lateinit var stompContext: Context is not initialized")
            return false
        }

        StompLifecycleService.stop(stompContext)
        return true
    }

    actual suspend fun collectStompEvent(onEvent: (Stomp.Event) -> Unit) {
        stomp.collectStompEvent { event ->
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