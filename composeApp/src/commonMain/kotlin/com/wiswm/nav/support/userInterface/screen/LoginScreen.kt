package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wiswm.nav.support.Permission
import com.wiswm.nav.support.resources.Colors
import com.wiswm.nav.support.resources.Strings
import com.wiswm.nav.support.userInterface.screen.common.OneButtonDialog
import com.wiswm.nav.support.userInterface.screen.common.ProgressBarView
import com.wiswm.nav.support.userInterface.viewModel.LoginViewModel
import com.wiswm.qr_code_scanner.CameraPermissionState
import com.wiswm.qr_code_scanner.CameraPosition
import com.wiswm.qr_code_scanner.CodeType
import com.wiswm.qr_code_scanner.ScannerWithPermissions
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_launcher_foreground
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

class LoginScreen() : Screen {
    companion object {
        const val TAG = "LoginScreen"
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val dashboardScreen: DashboardScreen = koinInject()
        val loginViewModel: LoginViewModel = koinInject()
        val snackbarHostState = remember { SnackbarHostState() }

        BackHandler(enabled = true) {
            if (loginViewModel.state.qrCodeScannerVisibilityStatus) {
                loginViewModel.setQrCodeScannerState(false)
            } else {
                navigator?.let {
                    if (it.canPop) {
                        it.pop()
                    }
                }
            }
        }

        Permission {
            Box(
                modifier = Modifier.background(Colors.PacificBlue)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize(),
                    containerColor = Colors.PacificBlue,
                    content = { padding ->
                        ContentView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            progressBarVisibilityStatus = loginViewModel.state.progressBarVisibilityStatus,
                            onClickConnectUser = {
                                loginViewModel.setQrCodeScannerState(true)
                            }
                        )
                    },
                    bottomBar = {
                        BottomView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 30.dp),
                            appVersion = loginViewModel.getAppVersion()
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                )

                QrCodeScannerView(
                    visibilityStatus = loginViewModel.state.qrCodeScannerVisibilityStatus,
                    onScanned = { message ->
                        loginViewModel.setQrCodeScannerState(false)
                        loginViewModel.login(message)
                    },
                    onClosed = {
                        loginViewModel.setQrCodeScannerState(false)
                    },
                    permissionDeniedContent = { permissionState ->
                        var visibilityStatus by remember { mutableStateOf(true) }
                        OneButtonDialog(
                            text = Strings.CAMERA_IS_REQUIRED_FOR_QR_CODE_SCANNING,
                            buttonText = Strings.OPEN_SETTINGS,
                            visibilityStatus = visibilityStatus,
                            onClick = {
                                visibilityStatus = false
                                permissionState.goToSettings()
                            }
                        )
                    }
                )
            }
        }

        key(loginViewModel.state.uiNotificationMessage) {
            LaunchedEffect(Unit) {
                loginViewModel.state.uiNotificationMessage?.let {
                    snackbarHostState.showSnackbar(it)
                }
            }
        }

        LaunchedEffect(Unit) {
            if (!loginViewModel.isAuthorizationRequired()) {
                navigator?.push(dashboardScreen)
                return@LaunchedEffect
            }

            loginViewModel.collectEvent { event ->
                when (event) {
                    LoginViewModel.Event.START_DASHBOARD_SCREEN -> {
                        navigator?.push(dashboardScreen)
                    }

                    else -> {}
                }
            }
        }
    }

    @Composable
    private fun ContentView(
        modifier: Modifier,
        progressBarVisibilityStatus: Boolean,
        onClickConnectUser: () -> Unit
    ) {
        val scrollState = rememberScrollState()
        Box(modifier = modifier) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Image(
                    painterResource(Res.drawable.ic_launcher_foreground),
                    "App logo",
                    Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
                Button(
                    modifier = Modifier
                        .size(288.dp, 40.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Colors.TexasRose,
                        contentColor = Color.Black
                    ),
                    enabled = !progressBarVisibilityStatus,
                    onClick = {
                        onClickConnectUser()
                    }
                ) {
                    Text(text = Strings.CONNECT_USER.uppercase())
                }
                ProgressBarView(progressBarVisibilityStatus)
            }
        }
    }

    @Composable
    private fun BottomView(modifier: Modifier, appVersion: String) {
        Box(
            modifier = modifier
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = Strings.APP_VERSION + appVersion, color = Color.White
            )
        }
    }

    @Composable
    private fun QrCodeScannerView(
        visibilityStatus: Boolean,
        onScanned: (message: String) -> Unit,
        onClosed: () -> Unit,
        permissionDeniedContent: @Composable (CameraPermissionState) -> Unit
    ) {
        if (!visibilityStatus) return

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            ScannerWithPermissions(
                onScanned = { message ->
                    onScanned(message)
                    return@ScannerWithPermissions true
                },
                types = listOf(
                    CodeType.Codabar,
                    CodeType.Code39,
                    CodeType.Code93,
                    CodeType.Code128,
                    CodeType.EAN8,
                    CodeType.EAN13,
                    CodeType.ITF,
                    CodeType.UPCE,
                    CodeType.Aztec,
                    CodeType.DataMatrix,
                    CodeType.PDF417,
                    CodeType.QR
                ),
                cameraPosition = CameraPosition.BACK,
                permissionDeniedContent = permissionDeniedContent
            )

            Button(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Colors.Blue),
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    onClosed()
                }
            ) {
                Text(text = Strings.CANCEL, color = Color.White)
            }
        }
    }
}