package com.wiswm.nav.support

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cafe.adriel.voyager.navigator.Navigator
import com.wiswm.nav.support.userInterface.screen.SplashScreen
import com.wiswm.nav.support.util.Logger
import org.koin.compose.koinInject

class BaseActivity : ComponentActivity() {
    companion object {
        const val TAG = "BaseActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate()")

        setContent {
            val splashScreen: SplashScreen = koinInject()
            Navigator(splashScreen)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy()")
    }
}