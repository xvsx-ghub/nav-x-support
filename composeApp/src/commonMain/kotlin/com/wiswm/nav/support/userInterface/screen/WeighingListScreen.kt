package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.wiswm.nav.support.Permission
import com.wiswm.nav.support.resources.Colors
import com.wiswm.nav.support.resources.Colors.Companion.NanoWhite
import com.wiswm.nav.support.resources.Strings
import com.wiswm.nav.support.userInterface.screen.common.MulticolorProgressBar
import com.wiswm.nav.support.userInterface.viewModel.WeighingListViewModel
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_arrow_right
import navxsupportapp.composeapp.generated.resources.ic_refresh
import navxsupportapp.composeapp.generated.resources.ic_weight
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.wiswm.nav.support.data.local.dataBase.entity.JobEntity
import org.koin.compose.koinInject

class WeighingListScreen() : Tab {
    companion object {
        const val TAG = "WeighingListScreen"
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
        val weighingListViewModel: WeighingListViewModel = koinInject()
        Permission {
            Box(modifier = Modifier.background(Colors.Black)) {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = Colors.PacificBlue,
                    topBar = {
                        TopView(
                            title = Strings.WEIGHINGS,
                            onRefreshClick = {
                                weighingListViewModel.getRemoteWeighingList()
                            }
                        )
                    },
                    content = { padding ->
                        weighingListViewModel.state.jobEntityList?.let {
                            ContentView(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding),
                                uncompletedCount = it.size,
                                jobEntityList = it.toList(),
                                weighingListViewModel = weighingListViewModel
                            )
                        }
                    },
                    bottomBar = {
                        BottomView(onClickStartNewWeighing = {
                            weighingListViewModel.saveJob(
                                jobEntity = JobEntity(
                                    type = JobEntity.Type.Unknown,
                                    completedStatus = false,
                                    timestamp = "",
                                    weighingIn = null,
                                    weighingOut = null,
                                    weighingNet = null
                                ),
                                onSaved = { jobEntity ->
                                    weighingListViewModel.setSelectedJobEntity(jobEntity)
                                    weighingListViewModel.showWeighingDetails()
                                }
                            )
                        })
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                )
                MulticolorProgressBar(visibilityStatus = weighingListViewModel.state.progressBarVisibilityStatus)
                key(weighingListViewModel.state.uiNotificationMessage) {
                    LaunchedEffect(Unit) {
                        weighingListViewModel.state.uiNotificationMessage?.let {
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
        uncompletedCount: Int,
        jobEntityList: List<JobEntity>,
        weighingListViewModel: WeighingListViewModel
    ) {
        val listState = rememberLazyListState()
        LazyColumn(
            modifier = modifier
                .background(Colors.White)
                .fillMaxSize()
                .padding(16.dp),
            state = listState,
        ) {
            item {
                UncompletedCountTextField(Strings.TOTAL_UNCOMPLETED + " $uncompletedCount")
                Spacer(modifier = Modifier.size(16.dp))
            }
            items(jobEntityList) { jobEntity ->
                val timestamp = try {
                    jobEntity.timestamp?.toLong()
                } catch (e: NumberFormatException) {
                    null
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    onClick = {
                        weighingListViewModel.setSelectedJobEntity(jobEntity)
                        weighingListViewModel.showWeighingDetails()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = weighingListViewModel
                                .getFormattedTimeSeconds(timestamp) ?: "",
                            onValueChange = {},
                            enabled = false,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = 16.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_right),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun UncompletedCountTextField(
        routeName: String,
        modifier: Modifier = Modifier
    ) {
        val shape = RoundedCornerShape(30.dp)
        val backgroundColor = Colors.SoftWhite
        val borderColor = Colors.Platinum

        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor, shape)
                .border(1.dp, borderColor, shape),
        ) {
            BasicTextField(
                value = routeName,
                onValueChange = {},
                enabled = false,
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(start = 16.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                    }
                }
            )
        }
    }

    @Composable
    private fun BottomView(onClickStartNewWeighing: () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Colors.White),
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Colors.Blue),
                shape = RoundedCornerShape(8.dp),
                onClick = {
                    onClickStartNewWeighing()
                }
            ) {
                Text(text = Strings.START_NEW_WEIGHING, color = Color.White)
            }
        }
    }
}