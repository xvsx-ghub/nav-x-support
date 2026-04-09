package com.wiswm.nav.support

import androidx.compose.ui.window.ComposeUIViewController
import cafe.adriel.voyager.navigator.Navigator
import com.wiswm.nav.support.dependencyInjection.initKoin
import com.wiswm.nav.support.userInterface.screen.SplashScreen
import org.koin.compose.koinInject

fun BaseViewController() = ComposeUIViewController {
    initKoin()
    val splashScreen: SplashScreen = koinInject()
    Navigator(splashScreen)
}