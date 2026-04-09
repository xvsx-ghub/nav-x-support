package com.wiswm.nav.support.data.remote.stomp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.wiswm.nav.support.util.Logger
import kotlinx.coroutines.delay
import org.hildan.krossbow.stomp.StompClient
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

class StompConnectionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object Companion {
        const val TAG = "StompConnectionWorker"
        const val RUN_MILLISECONDS = 1000L * 60 * 5
        const val WAIT_MILLISECONDS = 1000L * 60 * 5
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
            Logger.d(TAG, "start()")
            if (this.onError == null) this.onError = onError
            if (this.stomp == null) this.stomp = stomp
            if (this.stompClient == null) this.stompClient = stompClient
            if (this.stompConnectionDetails == null) this.stompConnectionDetails = stompConnectionDetails
            val request = OneTimeWorkRequest.Builder(StompConnectionWorker::class.java)
                .addTag(TAG)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun stop(context: Context) {
            Logger.d(TAG, "stop()")
            WorkManager.getInstance(context).cancelAllWorkByTag(TAG)
        }

        private fun preflightChecks(
            onSuccess: (
                stomp: Stomp,
                stompClient: StompClient,
                stompConnectionDetails: StompConnectionDetails,
                onError: (Exception) -> Unit) -> Unit) {
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

    //TODO Review and retest reconnection cycle. Could be broken.
    override suspend fun doWork(): Result {
        Logger.d(TAG, "doWork()")
        try {
            preflightChecks { stomp, stompClient, stompConnectionDetails, onError ->
                stomp.startListening(stompClient, stompConnectionDetails)
            }
            delay(RUN_MILLISECONDS)
            preflightChecks { stomp, stompClient, stompConnectionDetails, onError ->
                stomp.stopConnection()
            }
            WorkManager.getInstance(applicationContext).enqueue(
                OneTimeWorkRequestBuilder<StompConnectionWorker>()
                    .addTag(TAG)
                    .setInitialDelay(WAIT_MILLISECONDS, TimeUnit.MILLISECONDS)
                    .build()
            )
            return Result.success()
        } catch (e: CancellationException) {
            Logger.d(TAG, "doWork() CancellationException")
            preflightChecks { stomp, stompClient, stompConnectionDetails, onError ->
                stomp.stopConnection()
            }
            return Result.success()
        } catch (e: Exception) {
            Logger.e(TAG, "doWork() Exception", e)
            return Result.failure()
        }
    }
}