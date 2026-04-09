package com.wiswm.nav.support.data.remote.stomp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.wiswm.nav.support.R
import com.wiswm.nav.support.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient

class StompLifecycleService() : LifecycleService() {
    companion object Companion {
        const val TAG = "StompLifecycleService"
        private const val CHANNEL_ID = "stomp_lifecycle_service_channel"
        private const val NOTIFICATION_ID = 1
        var stoppedByTimeoutStatus: Boolean = false
        private var stomp: Stomp? = null
        private var stompClient: StompClient? = null
        private var stompConnectionDetails: StompConnectionDetails? = null
        private var onError: ((Exception) -> Unit)? = null

        fun start(
            context: Context,
            stomp: Stomp,
            stompClient: StompClient,
            stompConnectionDetails: StompConnectionDetails,
            onError: (Exception) -> Unit
        ) {
            if(this.onError == null) this.onError = onError
            if(this.stomp == null) this.stomp = stomp
            if(this.stompClient == null) this.stompClient = stompClient
            if(this.stompConnectionDetails == null) this.stompConnectionDetails = stompConnectionDetails

            val intent = Intent(context, StompLifecycleService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            stoppedByTimeoutStatus = false
            val intent = Intent(context, StompLifecycleService::class.java)
            context.stopService(intent)
        }

        fun appBackgroundModeEvent(context: Context) {
            ServiceTimeoutNotificationWorker.Companion.start(context)
        }

        fun appForegroundModeEvent(context: Context) {
            ServiceTimeoutNotificationWorker.Companion.stop(context)

            if (stoppedByTimeoutStatus) {
                preflightChecks { stomp, stompClient, stompConnectionDetails, onError ->
                    stoppedByTimeoutStatus = false
                    start(context, stomp, stompClient, stompConnectionDetails, onError)
                }
            }
        }

        private fun preflightChecks(onSuccess: (stomp: Stomp, stompClient: StompClient, stompConnectionDetails: StompConnectionDetails, onError: (Exception) -> Unit) -> Unit) {
            if (this.stomp == null || this.stompClient == null || this.stompConnectionDetails == null) {
                Logger.d(TAG, "appForegroundModeEvent() Error")
                this.onError?.let {
                    it(Exception("appForegroundModeEvent() Error"))
                }
                return
            }
            onSuccess(this.stomp!!, this.stompClient!!, this.stompConnectionDetails!!, this.onError!!)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "onCreate()")

        createNotificationChannelIfNeeded()
        startForeground(
            NOTIFICATION_ID,
            buildNotification(getString(R.string.stomp_service_starting))
        )

        preflightChecks { stomp, stompClient,stompConnectionDetails, onError ->
            lifecycleScope.launch {
                StompConnectionWorker.stop(applicationContext)
                delay(1000)
                stomp.startListening(stompClient,stompConnectionDetails)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy()")

        CoroutineScope(Dispatchers.IO).launch {
            preflightChecks { stomp, stompClient,stompConnectionDetails, onError ->
                stomp.stopConnection()
            }
            if(stoppedByTimeoutStatus) {
                delay(1000)
                preflightChecks { stomp, stompClient,stompConnectionDetails, onError ->
                    StompConnectionWorker.start(applicationContext, stomp, stompClient,stompConnectionDetails, onError)
                }
            }
        }
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        super.onTimeout(startId, fgsType)
        Logger.d(TAG, "onTimeout()")
        stopSelf()
        stoppedByTimeoutStatus = true
    }

    private fun buildNotification(text: String): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.technical_message))
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        return builder.build()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background service",
                NotificationManager.IMPORTANCE_LOW
            )
            nm.createNotificationChannel(channel)
        }
    }
}