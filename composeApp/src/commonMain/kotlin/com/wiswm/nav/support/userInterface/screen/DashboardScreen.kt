package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.wiswm.nav.support.Permission
import com.wiswm.nav.support.resources.Colors
import com.wiswm.nav.support.resources.Strings
import com.wiswm.nav.support.userInterface.screen.common.MulticolorProgressBar
import com.wiswm.nav.support.userInterface.screen.common.OneButtonDialog
import com.wiswm.nav.support.userInterface.viewModel.DashboardViewModel
import com.wiswm.qr_code_scanner.CameraPosition
import com.wiswm.qr_code_scanner.CodeType
import com.wiswm.qr_code_scanner.ScannerWithPermissions
import org.koin.compose.koinInject

class DashboardScreen() : Screen {
    companion object {
        const val TAG = "DashboardScreen"
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val dashboardViewModel: DashboardViewModel = koinInject()
        val taskListTab: TaskListScreen = koinInject()
        val customerListTab: CustomerListScreen = koinInject()
        val reportTab: ReportScreen = koinInject()
        val weighingListTab: WeighingListScreen = koinInject()
        val profileTab: ProfileScreen = koinInject()
        val customerDetailsScreen: CustomerDetailsScreen = koinInject()
        val weighingDetailsScreen: WeighingDetailsScreen = koinInject()

        BackHandler(enabled = true) {
            dashboardViewModel.manageQrCodeScanner(false, null)
        }

        LaunchedEffect(Unit) {
            dashboardViewModel.collectEvent { event ->
                when (event) {
                    DashboardViewModel.Event.GET_SIGNATURE -> {
                        val drawingPadModuleScreen = DrawingPadModuleScreen(
                            taskTag = dashboardViewModel.state.targetTab.name,
                            onClose = { taskTag, photoPath ->
                                navigator?.pop()
                                photoPath?.let {
                                    dashboardViewModel.serveDrawingPadScreen(taskTag, it)
                                }
                            })
                        navigator?.push(drawingPadModuleScreen)
                    }

                    DashboardViewModel.Event.GET_PHOTO -> {
                        val cameraModuleScreen = CameraModuleScreen(
                            taskTag = dashboardViewModel.state.targetTab.name,
                            onClose = { taskTag, photoPath ->
                                navigator?.pop()
                                photoPath?.let {
                                    dashboardViewModel.serveCameraScreen(taskTag, it)
                                }
                            }
                        )
                        navigator?.push(cameraModuleScreen)
                    }

                    DashboardViewModel.Event.GET_QR_CODE -> {
                        dashboardViewModel.manageQrCodeScanner(true)
                    }

                    DashboardViewModel.Event.LOG_OUT -> {
                        navigator?.pop()
                    }

                    DashboardViewModel.Event.LAUNCH_CUSTOMER_DETAILS -> {
                        navigator?.push(customerDetailsScreen)
                    }

                    DashboardViewModel.Event.LAUNCH_WEIGHING_DETAILS -> {
                        navigator?.push(weighingDetailsScreen)
                    }

                    else -> {}
                }
            }
        }

        Permission {
            Box(modifier = Modifier.background(Colors.Black).fillMaxSize()) {
                val snackbarHostState = remember { SnackbarHostState() }

                TabNavigator(taskListTab) { tabNavigator ->
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                        containerColor = Colors.PacificBlue,
                        content = { padding ->
                            Box(modifier = Modifier.padding(padding)) {
                                CurrentTab()
                            }
                        },
                        bottomBar = {
                            val density = LocalDensity.current
                            val keyboardVisible =
                                WindowInsets.ime.getBottom(density) > 0
                            if (!keyboardVisible) {
                                BottomView(
                                    tabNavigator,
                                    taskListTab,
                                    customerListTab,
                                    reportTab,
                                    weighingListTab,
                                    profileTab
                                )
                            }
                        },
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    )
                    QrCodeScannerView(
                        visibilityStatus = dashboardViewModel.state.qrCodeScannerVisibilityStatus,
                        onScanned = { message ->
                            dashboardViewModel.manageQrCodeScanner(false, message)
                        },
                        onClosed = {
                            dashboardViewModel.manageQrCodeScanner(false)
                        }
                    )
                    MulticolorProgressBar(dashboardViewModel.state.progressBarVisibilityStatus)
                    key(dashboardViewModel.state.uiNotificationMessage) {
                        LaunchedEffect(Unit) {
                            dashboardViewModel.state.uiNotificationMessage?.let {
                                snackbarHostState.showSnackbar(it)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BottomView(
        tabNavigator: TabNavigator,
        taskListTab: TaskListScreen,
        customerListTab: CustomerListScreen,
        reportTab: ReportScreen,
        weighingListTab: WeighingListScreen,
        profileTab: ProfileScreen
    ) {
        NavigationBar {
            NavigationBarItem(
                selected = tabNavigator.current == taskListTab,
                onClick = { tabNavigator.current = taskListTab },
                label = { Text(taskListTab.options.title, color = Colors.RoyalBlue) },
                icon = {
                    taskListTab.options.icon?.let {
                        Icon(
                            it,
                            "",
                            tint = Color.Unspecified
                        )
                    }
                }
            )

            NavigationBarItem(
                selected = tabNavigator.current == customerListTab,
                onClick = { tabNavigator.current = customerListTab },
                label = { Text(customerListTab.options.title, color = Colors.RoyalBlue) },
                icon = {
                    customerListTab.options.icon?.let {
                        Icon(
                            it,
                            "",
                            tint = Color.Unspecified
                        )
                    }
                }
            )

            NavigationBarItem(
                selected = tabNavigator.current == reportTab,
                onClick = { tabNavigator.current = reportTab },
                label = { Text(reportTab.options.title, color = Colors.RoyalBlue) },
                icon = {
                    reportTab.options.icon?.let {
                        Icon(
                            it,
                            "",
                            tint = Color.Unspecified
                        )
                    }
                }
            )

            NavigationBarItem(
                selected = tabNavigator.current == weighingListTab,
                onClick = { tabNavigator.current = weighingListTab },
                label = { Text(weighingListTab.options.title, color = Colors.RoyalBlue) },
                icon = {
                    weighingListTab.options.icon?.let {
                        Icon(
                            it,
                            "",
                            tint = Color.Unspecified
                        )
                    }
                }
            )

            NavigationBarItem(
                selected = tabNavigator.current == profileTab,
                onClick = { tabNavigator.current = profileTab },
                label = { Text(profileTab.options.title, color = Colors.RoyalBlue) },
                icon = {
                    profileTab.options.icon?.let {
                        Icon(
                            it,
                            "",
                            tint = Color.Unspecified
                        )
                    }
                }
            )
        }
    }

    @Composable
    private fun QrCodeScannerView(
        visibilityStatus: Boolean,
        onScanned: (message: String) -> Unit,
        onClosed: () -> Unit
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