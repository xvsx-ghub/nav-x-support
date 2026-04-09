package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wiswm.nav.support.Permission
import com.wiswm.nav.support.resources.Colors
import com.wiswm.nav.support.resources.Strings
import navxsupportapp.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wiswm.nav.support.data.local.dataBase.entity.CustomerTaskEntity
import com.wiswm.nav.support.data.remote.stomp.Stomp
import com.wiswm.nav.support.userInterface.screen.common.MulticolorProgressBar
import com.wiswm.nav.support.userInterface.screen.common.NotServicingReasonAlertDialog
import com.wiswm.nav.support.userInterface.viewModel.CustomerDetailsViewModel
import navxsupportapp.composeapp.generated.resources.ic_arrow_back
import navxsupportapp.composeapp.generated.resources.ic_problem
import org.koin.compose.koinInject

class CustomerDetailsScreen() :
    Screen {
    companion object {
        const val TAG = "CustomerDetailsScreen"
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val customerDetailsViewModel: CustomerDetailsViewModel = koinInject()

        Permission {
            Box(modifier = Modifier.background(Colors.Black)) {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = Colors.White,
                    topBar = {
                        TopView(
                            title = customerDetailsViewModel
                                .state.customerEntity?.customerRefId ?: "",
                            onClickBack = {
                                navigator?.pop()
                            },
                            stompConnectionEvent = customerDetailsViewModel
                                .state.stompConnectionEvent
                        )
                    },
                    content = { padding ->
                        ContentView(
                            modifier = Modifier.padding(padding),
                            onClickReport = { customerTaskEntity ->
                                customerDetailsViewModel.setSelectedCustomerEntity(
                                    customerId = customerTaskEntity.customerId,
                                    onComplete = { status ->
                                        if (status) customerDetailsViewModel
                                            .pushNotServicingReasonDialog(true)
                                    }
                                )
                            },
                            customerDetailsViewModel = customerDetailsViewModel
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                )

                key(customerDetailsViewModel.state.notServicingReasonDialogVisibilityStatus) {
                    NotServicingReasonAlertDialog(
                        notServicingReasonList = customerDetailsViewModel
                            .state.notServicingReasonEntityList,
                        visibilityStatus = customerDetailsViewModel
                            .state.notServicingReasonDialogVisibilityStatus,
                        onDismiss = {
                            customerDetailsViewModel.pushNotServicingReasonDialog(false)
                        },
                        onConfirm = { notServicingReasonEntity ->
                            customerDetailsViewModel.pushNotServicingReasonDialog(false)
                            customerDetailsViewModel.createNotServicingReasonPhotoTask(notServicingReasonEntity)
                        },
                        onError = { message ->
                            customerDetailsViewModel.pushNotServicingReasonDialog(false)
                            customerDetailsViewModel.setUiNotification(message)
                        }
                    )
                }

                MulticolorProgressBar(
                    modifier = Modifier.fillMaxSize(),
                    visibilityStatus = customerDetailsViewModel.state.progressBarVisibilityStatus
                )

                LaunchedEffect(Unit) {
                    customerDetailsViewModel.collectEvent { event ->
                        when (event) {
                            CustomerDetailsViewModel.Event.GET_PHOTO -> {
                                val cameraModuleScreen = CameraModuleScreen(
                                    taskTag = TAG,
                                    onClose = { taskTag, photoPath ->
                                        navigator?.pop()
                                        photoPath?.let {
                                            customerDetailsViewModel.serveCameraScreen(taskTag, it)
                                        }
                                    }
                                )
                                navigator?.push(cameraModuleScreen)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TopView(title: String, onClickBack: () -> Unit, stompConnectionEvent: Stomp.Event) {
        val color = when (stompConnectionEvent) {
            Stomp.Event.UNKNOWN -> Color.Gray
            Stomp.Event.CONNECTED -> Color.Green
            else -> Color.Red
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Colors.NanoWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier.size(32.dp),
                        onClick = { onClickBack() }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Exit",
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }

    @Composable
    private fun ContentView(
        modifier: Modifier,
        onClickReport: (customerTaskEntity: CustomerTaskEntity) -> Unit,
        customerDetailsViewModel: CustomerDetailsViewModel
    ) {
        key(customerDetailsViewModel.state.updateUiTrigger) {
            customerDetailsViewModel.state.customerTaskEntityList?.let {
                LazyColumn(modifier = modifier) {
                    items(it) { customerTaskEntity ->
                        CustomerTaskListItem(
                            customerTaskEntity = customerTaskEntity,
                            onClickReport = { customerTaskEntity ->
                                onClickReport(customerTaskEntity)
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun CustomerTaskListItem(
        customerTaskEntity: CustomerTaskEntity,
        onClickReport: (customerTaskEntity: CustomerTaskEntity) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Colors.NanoWhite
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp),
            ) {
                Row {
                    Column {
                        Text(
                            text = "Type:",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "RFID:",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Bar code:",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Column {
                        Text(
                            text = customerTaskEntity.binType,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = customerTaskEntity.chipCode,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = customerTaskEntity.barcode,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Colors.Carnation),
                    onClick = {
                        onClickReport(customerTaskEntity)
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_problem),
                        contentDescription = null,
                        tint = Colors.WhiteLiliac,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = Strings.REPORT_A_PROBLEM, color = Colors.WhiteLiliac)
                }
            }
        }
    }
}