package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.layout.ContentScale
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wiswm.nav.support.Permission
import com.wiswm.nav.support.resources.Colors
import com.wiswm.nav.support.userInterface.viewModel.SplashViewModel
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_launcher_foreground
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

class SplashScreen() : Screen {
    companion object {
        const val TAG = "SplashScreen"
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val splashViewModel: SplashViewModel = koinInject()
        val navigator = LocalNavigator.current
        val loginScreen: LoginScreen= koinInject()
        val dashboardScreen: DashboardScreen = koinInject()

        BackHandler(enabled = true) {}

        LaunchedEffect(Unit) {
            if (splashViewModel.isAuthorizationRequired()) {
                navigator?.push(loginScreen)
            }else{
                navigator?.push(dashboardScreen)
            }
        }

        Permission {
            Box(
                modifier = Modifier.background(Colors.PacificBlue)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                ContentView()
            }
        }
    }

    @Composable
    private fun ContentView(
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_launcher_foreground),
                contentDescription = "App logo",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }
    }
}