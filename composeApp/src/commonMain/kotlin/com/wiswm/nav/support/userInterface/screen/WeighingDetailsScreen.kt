package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.wiswm.nav.support.Permission
import com.wiswm.nav.support.resources.Colors
import com.wiswm.nav.support.resources.Colors.Companion.NanoWhite
import com.wiswm.nav.support.resources.Strings
import com.wiswm.nav.support.userInterface.screen.common.MulticolorProgressBar
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_weight
import org.jetbrains.compose.resources.painterResource
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wiswm.nav.support.data.local.dataBase.entity.Weighing
import com.wiswm.nav.support.userInterface.screen.common.Button
import com.wiswm.nav.support.userInterface.screen.common.OneButtonDialog
import com.wiswm.nav.support.userInterface.viewModel.WeighingDetailsViewModel
import navxsupportapp.composeapp.generated.resources.ic_arrow_back
import com.wiswm.nav.support.userInterface.screen.common.Spinner
import com.wiswm.nav.support.userInterface.screen.common.TwoButtonsDialog
import com.wiswm.nav.support.userInterface.screen.common.WeightText
import com.wiswm.nav.support.userInterface.screen.common.WeightTextType
import navxsupportapp.composeapp.generated.resources.ic_attention_circle
import navxsupportapp.composeapp.generated.resources.ic_cross_in_circle
import navxsupportapp.composeapp.generated.resources.ic_photo
import navxsupportapp.composeapp.generated.resources.ic_report_completed
import org.koin.compose.koinInject

class WeighingDetailsScreen() : Tab {
    companion object {
        const val TAG = "WeighingDetailsScreen"
    }

    override val options: TabOptions
        @Composable get() {
            return TabOptions(
                index = 3u,
                title = Strings.WEIGHINGS,
                icon = painterResource(Res.drawable.ic_weight)
            )
        }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val weighingDetailsViewModel: WeighingDetailsViewModel = koinInject()
        Permission {
            Box(modifier = Modifier.background(Colors.Black)) {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = Colors.White,
                    topBar = {
                        val timestamp = try {
                            weighingDetailsViewModel.state.jobEntity?.timestamp?.toLong()
                        } catch (e: NumberFormatException) {
                            null
                        }

                        TopView(
                            title = weighingDetailsViewModel.getFormattedTimeSeconds(timestamp)
                                ?: "",
                            onClickBack = {
                                weighingDetailsViewModel.clearData()
                                navigator?.pop()
                            }
                        )
                    },
                    content = { padding ->
                        ContentView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            onExit = {
                                weighingDetailsViewModel.clearData()
                                navigator?.pop()
                            },
                            weighingDetailsViewModel = weighingDetailsViewModel
                        )
                    },
                    bottomBar = {
                        val density = LocalDensity.current
                        val keyboardVisible = WindowInsets.ime.getBottom(density) > 0
                        if (!keyboardVisible) {
                            BottomView(
                                weighingDetailsViewModel = weighingDetailsViewModel,
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                )
                MulticolorProgressBar(visibilityStatus = weighingDetailsViewModel.state.progressBarVisibilityStatus)
                key(weighingDetailsViewModel.state.uiNotificationMessage) {
                    LaunchedEffect(Unit) {
                        weighingDetailsViewModel.state.uiNotificationMessage?.let {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            weighingDetailsViewModel.collectEvent { event ->
                when (event) {
                    WeighingDetailsViewModel.Event.GET_PHOTO -> {
                        val cameraModuleScreen = CameraModuleScreen(
                            taskTag = TAG,
                            onClose = { taskTag, photoPath ->
                                navigator?.pop()
                                photoPath?.let {
                                    if (taskTag == TAG) weighingDetailsViewModel.setPhoto(it)
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

    @Composable
    private fun TopView(title: String, onClickBack: () -> Unit) {
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
                            contentDescription = null,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    @Composable
    private fun ContentView(
        modifier: Modifier,
        onExit: () -> Unit,
        weighingDetailsViewModel: WeighingDetailsViewModel
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val annotatedText = buildAnnotatedString {
            append(Strings.CONFIRM_THE_INDICATED_WEIGHT_IN)
            withStyle(
                style = SpanStyle(color = Color.Blue)
            ) {
                append("${weighingDetailsViewModel.state.weighing?.weightValue}")
                append(" ")
                append(Strings.KG)
            }
        }
        Column(
            modifier = modifier.verticalScroll(rememberScrollState())
                .background(Colors.White)
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                .padding(16.dp),
        ) {
            weighingDetailsViewModel.state.destinationEntityList?.let { nnDestinationEntityList ->
                Spinner(
                    options = nnDestinationEntityList.map { mapEntry -> mapEntry.mapToSpinnerItem() },
                    onOptionSelected = { option ->
                        nnDestinationEntityList.find { destinationEntity -> destinationEntity.remoteId.toLong() == option.id }
                            ?.let {
                                weighingDetailsViewModel.setWeightValue(null)
                                weighingDetailsViewModel.setWeighbridge(null)
                                weighingDetailsViewModel.setDestination(it)
                            }
                    },
                    hint = Strings.DESTINATION,
                    selectedOptionIndex = nnDestinationEntityList.indexOf(
                        weighingDetailsViewModel.state.selectedDestinationEntity
                    ),
                    enableStatus = weighingDetailsViewModel.state.destinationSpinnerEnableStatus
                )
            }
            if (weighingDetailsViewModel.state.weighbridgeSectionVisibilityStatus) {
                Spacer(Modifier.size(8.dp))
                weighingDetailsViewModel.state.selectedDestinationEntity?.weighbridgeList?.let { nnWeighbridgeList ->
                    Spinner(
                        options = nnWeighbridgeList.map { mapEntry -> mapEntry.mapToSpinnerItem() },
                        onOptionSelected = { option ->
                            nnWeighbridgeList.find { wasteTypeEntity -> wasteTypeEntity.remoteId.toLong() == option.id }
                                ?.let {
                                    weighingDetailsViewModel.setWeightValue(null)
                                    weighingDetailsViewModel.setWeighbridge(it)
                                }
                        },
                        hint = Strings.WEIGHBRIDGE,
                        selectedOptionIndex = nnWeighbridgeList.indexOf(
                            weighingDetailsViewModel.state.selectedWeighbridge
                        ),
                        enableStatus = weighingDetailsViewModel.state.weighbridgeSpinnerEnableStatus
                    )
                }
            }
            Spacer(Modifier.size(8.dp))
            weighingDetailsViewModel.state.wasteTypeEntityList?.let { nnWasteTypeEntityList ->
                Spinner(
                    options = nnWasteTypeEntityList.map { mapEntry -> mapEntry.mapToSpinnerItem() },
                    onOptionSelected = { option ->
                        nnWasteTypeEntityList.find { wasteTypeEntity -> wasteTypeEntity.remoteId.toLong() == option.id }
                            ?.let {
                                weighingDetailsViewModel.setWasteType(it)
                            }
                    },
                    hint = Strings.WASTE_TYPE,
                    selectedOptionIndex = nnWasteTypeEntityList.indexOf(
                        weighingDetailsViewModel.state.selectedWasteTypeEntity
                    ),
                    enableStatus = weighingDetailsViewModel.state.wasteTypeSpinnerEnableStatus
                )
            }
            if (weighingDetailsViewModel.state.weightSectionVisibilityStatus) {
                Spacer(Modifier.size(16.dp))

                val weighingType = when (weighingDetailsViewModel.state.weighing?.direction) {
                    Weighing.Direction.In -> {
                        when (weighingDetailsViewModel.state.weighing?.type) {
                            Weighing.Type.Manual -> WeightTextType.ManualIn
                            Weighing.Type.Automatic -> WeightTextType.AutomaticIn
                            else -> WeightTextType.Unknown
                        }
                    }

                    Weighing.Direction.Out -> {
                        when (weighingDetailsViewModel.state.weighing?.type) {
                            Weighing.Type.Manual -> WeightTextType.ManualOut
                            Weighing.Type.Automatic -> WeightTextType.AutomaticOut
                            else -> WeightTextType.Unknown
                        }
                    }

                    Weighing.Direction.Net -> {
                        when (weighingDetailsViewModel.state.weighing?.type) {
                            Weighing.Type.Manual -> WeightTextType.ManualNet
                            Weighing.Type.Automatic -> WeightTextType.AutomaticNet
                            else -> WeightTextType.Unknown
                        }
                    }

                    else -> WeightTextType.Unknown
                }

                var alertMessage by remember { mutableStateOf<String?>(null) }
                WeightText(
                    value = weighingDetailsViewModel.state.weighing?.weightValue ?: "",
                    onValueChanged = {
                        weighingDetailsViewModel.setWeightValue(it)
                    },
                    type = weighingType,
                    onRefreshClick = {
                        weighingDetailsViewModel.refreshWeight(
                            onRefreshStarted = {

                            },
                            onRefreshCompleted = {
                                alertMessage = null
                            },
                            onRefreshBusy = {
                                alertMessage = Strings.YOU_CAN_NOT_PRESS_REFRESH
                            }
                        )
                    },
                    lazyStatus = weighingDetailsViewModel.state.refreshButtonBusyStatus
                )
                Spacer(Modifier.size(8.dp))
                AlertView(alertMessage)
                Spacer(Modifier.size(8.dp))
                DocketView(
                    photoPath = weighingDetailsViewModel.state.photoPath,
                    onTakePhotoClick = {
                        weighingDetailsViewModel.getPhoto()
                    },
                    onDeletePhoto = {
                        weighingDetailsViewModel.deletePhoto()
                    }
                )
            }
        }
        OneButtonDialog(
            text = Strings.TO_COMPLETE_THE_WEIGHING_,
            buttonText = Strings.OPEN_CAMERA,
            visibilityStatus = weighingDetailsViewModel.state.photoRequiredDialogVisibilityStatus,
            onClick = {
                weighingDetailsViewModel.pushPhotoRequiredDialog(false)
                weighingDetailsViewModel.getPhoto()
            }
        )
        OneButtonDialog(
            icon = Res.drawable.ic_report_completed,
            text = Strings.THE_DATA_HAS_BEEN_SUCCESSFULLY_SUBMITTED,
            buttonText = Strings.DONE_,
            visibilityStatus = weighingDetailsViewModel.state.successDialogVisibilityStatus,
            onClick = {
                weighingDetailsViewModel.pushSuccessDialog(false)
                onExit()
            },
            title = Strings.GREAT_
        )
        OneButtonDialog(
            //icon = Res.drawable.ic_report_completed,
            //text = Strings.DATA_SUBMITTING_ERROR,
            buttonText = Strings.DONE_,
            visibilityStatus = weighingDetailsViewModel.state.errorDialogVisibilityStatus,
            onClick = {
                weighingDetailsViewModel.pushErrorDialog(false)
                onExit()
            },
            title = Strings.DATA_SUBMITTING_ERROR
        )
        TwoButtonsDialog(
            text = annotatedText,
            visibilityStatus = weighingDetailsViewModel.state.confirmationDialogVisibilityStatus,
            onCancel = {
                weighingDetailsViewModel.pushConfirmationDialog(false)
            },
            onConfirm = {
                weighingDetailsViewModel.pushConfirmationDialog(false)
                weighingDetailsViewModel.submitWeighing()
            },
            title = Strings.WEIGHT_IN_CONFIRMATION
        )
    }

    @Composable
    private fun BottomView(weighingDetailsViewModel: WeighingDetailsViewModel) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Colors.White).padding(16.dp),
        ) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    if (weighingDetailsViewModel.state.photoPath.isNullOrEmpty())
                        weighingDetailsViewModel.pushPhotoRequiredDialog(true)
                    else
                        weighingDetailsViewModel.pushConfirmationDialog(true)
                },
                text = Strings.SUBMIT_WEIGHING,
                enableStatus = weighingDetailsViewModel.state.dataValidationStatus
            )
        }
    }

    @Composable
    private fun AlertView(
        message: String?
    ) {
        message?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Colors.Red, shape = RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_attention_circle),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
                Spacer(Modifier.size(8.dp))
                Text(text = message, color = Colors.Red)
            }
        }
    }

    @Composable
    private fun DocketView(
        photoPath: String? = null,
        onTakePhotoClick: (() -> Unit)? = null,
        onDeletePhoto: (() -> Unit)? = null
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (photoPath.isNullOrEmpty()) {
                        Column {
                            Text(Strings.PHOTO_OF_DOCKET)
                            Spacer(Modifier.size(8.dp))
                            Row {
                                Text(text = Strings.REQUIRED, color = Colors.Red)
                                Spacer(Modifier.size(8.dp))
                                Icon(
                                    painter = painterResource(Res.drawable.ic_attention_circle),
                                    contentDescription = null,
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = photoPath.substringAfterLast("/"),
                                modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis
                            )

                            IconButton(
                                modifier = Modifier
                                    .size(32.dp),
                                onClick = {
                                    onDeletePhoto?.invoke()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_cross_in_circle),
                                    contentDescription = null,
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.size(32.dp))
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = {
                            onTakePhotoClick?.let {
                                it()
                            }
                        },
                        text = Strings.TAKE_PHOTO,
                        image = Res.drawable.ic_photo
                    )
                }
            }
        }
    }
}