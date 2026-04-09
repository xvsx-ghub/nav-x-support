package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.wiswm.nav.support.Permission
import com.wiswm.nav.support.resources.Colors
import com.wiswm.nav.support.resources.Colors.Companion.BlueRibbon
import com.wiswm.nav.support.resources.Colors.Companion.NanoWhite
import com.wiswm.nav.support.resources.Strings
import com.wiswm.nav.support.userInterface.viewModel.ReportViewModel
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_delete
import navxsupportapp.composeapp.generated.resources.ic_refresh
import navxsupportapp.composeapp.generated.resources.ic_report
import navxsupportapp.composeapp.generated.resources.ic_send_report
import org.jetbrains.compose.resources.painterResource
import androidx.compose.runtime.setValue
import navxsupportapp.composeapp.generated.resources.ic_checked
import navxsupportapp.composeapp.generated.resources.ic_photo
import navxsupportapp.composeapp.generated.resources.ic_problem
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.wiswm.nav.support.data.local.dataBase.entity.TruckReportEntity
import com.wiswm.nav.support.userInterface.screen.common.MulticolorProgressBar
import com.wiswm.nav.support.userInterface.screen.common.TwoButtonsDialog
import com.wiswm.nav.support.userInterface.screen.common.OneButtonDialog
import com.wiswm.nav.support.util.Logger
import navxsupportapp.composeapp.generated.resources.ic_report_completed
import org.koin.compose.koinInject

class ReportScreen() : Tab {
    companion object {
        const val TAG = "ReportScreen"
    }

    override val options: TabOptions
        @Composable get() {
            return TabOptions(
                index = 2u,
                title = Strings.REPORT,
                icon = painterResource(Res.drawable.ic_report)
            )
        }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val reportViewModel: ReportViewModel = koinInject()

        Permission {
            Box(modifier = Modifier.background(Colors.Black)) {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing.exclude(WindowInsets.ime)),
                    containerColor = Colors.PacificBlue,
                    topBar = {
                        TopView(
                            title = Strings.TRUCK_REPORT,
                            onRefreshClick = {
                                reportViewModel.getTruckReportList()
                            }
                        )
                    },
                    content = { padding ->
                        ContentView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            reportViewModel = reportViewModel
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                )
                MulticolorProgressBar(visibilityStatus = reportViewModel.state.progressBarVisibilityStatus)
                key(reportViewModel.state.uiNotificationMessage) {
                    LaunchedEffect(Unit) {
                        reportViewModel.state.uiNotificationMessage?.let {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TopView(title: String, onRefreshClick: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NanoWhite)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    modifier = Modifier
                        .background(NanoWhite)
                        .size(32.dp),
                    onClick = {
                        onRefreshClick()
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_refresh),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }

    @Composable
    private fun ContentView(
        modifier: Modifier,
        reportViewModel: ReportViewModel
    ) {
        Column(
            modifier = modifier.background(Colors.White)
                .fillMaxSize()
        ) {
            key(reportViewModel.state.updateUiTrigger) {
                Logger.d(TAG, "reportViewModel.state.updateUiTrigger")
                reportViewModel.state.truckReportEntityList?.let {
                    TruckReportParamList(
                        truckReportEntityList = it,
                        onSendReportButtonClick = {
                            reportViewModel.pushTwoButtonsAlertDialog(true)
                        },
                        reportViewModel = reportViewModel
                    )
                }
            }
        }

        val annotatedText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(color = Color.Black)
            ) {
                append(Strings.ARE_YOU_SURE_YOU_HAVE_CHECKED_ALL_THE_ITEMS_MENTIONED_IN_THE_REPORT)
            }
        }

        TwoButtonsDialog(
            text = annotatedText,
            visibilityStatus = reportViewModel.state.twoButtonsAlertDialogVisibilityStatus,
            onCancel = {
                reportViewModel.pushTwoButtonsAlertDialog(false)

            },
            onConfirm = {
                reportViewModel.pushTwoButtonsAlertDialog(false)
                reportViewModel.sendTruckReport()
            }
        )
        OneButtonDialog(
            icon = Res.drawable.ic_report_completed,
            title = Strings.GREAT_,
            text = Strings.TRUCK_REPORT_IS_SUCCESSFULLY_SENT,
            buttonText = Strings.CONFIRM,
            visibilityStatus = reportViewModel.state.oneButtonAlertDialogVisibilityStatus,
            onClick = {
                reportViewModel.pushOneButtonAlertDialog(false)
            }
        )
    }

    @Composable
    fun TruckReportParamList(
        truckReportEntityList: List<TruckReportEntity>,
        onSendReportButtonClick: () -> Unit,
        reportViewModel: ReportViewModel
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .imePadding()
                    .background(Transparent)
                    .padding(16.dp)
            ) {
                items(truckReportEntityList) { truckReportEntity: TruckReportEntity ->
                    TruckReportParamItemContent(
                        truckReportEntity = truckReportEntity,
                        reportViewModel = reportViewModel
                    )
                }
                item {
                    SendReportButton(
                        onClick = {
                            onSendReportButtonClick()
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun TruckReportParamItemContent(
        truckReportEntity: TruckReportEntity,
        reportViewModel: ReportViewModel
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TruckReportParamItemContentMain(truckReportEntity = truckReportEntity, reportViewModel = reportViewModel)
        }
    }

    @Composable
    fun TruckReportParamItemContentMain(
        truckReportEntity: TruckReportEntity,
        reportViewModel: ReportViewModel
    ) {
        var text by remember { mutableStateOf(truckReportEntity.value ?: "") }
        var photoName by remember { mutableStateOf(truckReportEntity.photoPath) }
        val focusManager = LocalFocusManager.current
        Row(
            modifier = Modifier
                .background(Transparent)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .shadow(1.dp)
                .clickable {
                    reportViewModel.updateTruckReportEntityCheckedStatus(
                        truckReportEntity = truckReportEntity,
                        checkedStatus = !truckReportEntity.checkedStatus
                    )
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (truckReportEntity.checkedStatus) {
                Column {
                    Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))) {
                        Icon(
                            modifier = Modifier
                                .background(Colors.Red)
                                .padding(8.dp),
                            painter = painterResource(Res.drawable.ic_problem),
                            tint = White,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        modifier = Modifier
                            .background(Transparent),
                        text = truckReportEntity.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = text,
                        onValueChange = {
                            text = it
                            reportViewModel.updateTruckReportEntityValue(
                                truckReportEntity,
                                text
                            )
                        },
                        label = { Text("Problem description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gray,
                            unfocusedBorderColor = Gray,
                            disabledBorderColor = Gray,
                            errorBorderColor = Gray,
                            focusedLabelColor = Gray,
                            unfocusedLabelColor = Gray,
                            cursorColor = Gray
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        )
                    )
                    Spacer(modifier = Modifier.heightIn(8.dp))
                    Row(
                        modifier = Modifier
                            .background(Transparent)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))) {
                            Icon(
                                modifier = Modifier
                                    .background(Colors.Blue)
                                    .clickable {
                                        photoName = ""
                                        reportViewModel.setSelectedTruckReportEntity(
                                            truckReportEntity
                                        )
                                        reportViewModel.getPhoto()
                                    }
                                    .padding(16.dp),
                                painter = painterResource(resource = Res.drawable.ic_photo),
                                tint = White,
                                contentDescription = null
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .background(Transparent),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (truckReportEntity.photoPath.isNullOrEmpty()) {
                                photoName = Strings.NO_PHOTO_SUBMITTED
                            }
                            Text(
                                text = photoName!!,
                                modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.headlineSmall,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                modifier = Modifier
                                    .clickable {
                                        photoName = ""
                                        reportViewModel.deleteTruckReportEntityPhotoPath(
                                            truckReportEntity
                                        )
                                    },
                                painter = painterResource(resource = Res.drawable.ic_delete),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp))) {
                    Icon(
                        modifier = Modifier
                            .background(Colors.Green)
                            .padding(8.dp),
                        painter = painterResource(Res.drawable.ic_checked),
                        tint = White,
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier
                        .background(Transparent),
                    text = truckReportEntity.name,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }

    @Composable
    fun SendReportButton(onClick: () -> Unit) {
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = BlueRibbon),
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_send_report),
                contentDescription = null,
                tint = White,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = Strings.SEND_REPORT, color = White)
        }
    }
}