package com.wiswm.nav.support

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.wiswm.nav.support.dependencyInjection.initKoin
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import com.wiswm.camera.cameraContext
import com.wiswm.drawing_pad.drawingPadContext
import com.wiswm.nav.support.data.remote.stomp.StompLifecycleService
import com.wiswm.nav.support.util.Logger
import com.wiswm.nav.support.data.remote.stomp.stompContext
import com.wiswm.nav.support.util.connectivityObserverContext
import roomContext

class BaseApplication : Application(){
    companion object Companion {
        const val TAG = "BaseApplication"
    }

    override fun onCreate() {
        super.onCreate()
        roomContext = this
        stompContext = this
        cameraContext = this
        drawingPadContext = this
        connectivityObserverContext = this

        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

        WorkManager.initialize(this, config)

        initKoin()
        setLifecycleEventObserver(this)
    }

    private fun setLifecycleEventObserver(context: Context) {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> {
                        Logger.d(TAG, "Lifecycle.Event.ON_STOP")
                        StompLifecycleService.appBackgroundModeEvent(context)
                    }

                    Lifecycle.Event.ON_START -> {
                        Logger.d(TAG, "Lifecycle.Event.ON_START")
                        StompLifecycleService.appForegroundModeEvent(context)
                    }

                    Lifecycle.Event.ON_CREATE -> {
                        Logger.d(TAG, "Lifecycle.Event.ON_CREATE")
                        WorkManager.getInstance(this).cancelAllWork()
                    }

                    else -> {}
                }
            }
        )
    }
}