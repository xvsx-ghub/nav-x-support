package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.wiswm.nav.support.Permission
import com.wiswm.nav.support.data.local.dataBase.entity.TaskEntity
import com.wiswm.nav.support.data.remote.stomp.Stomp
import com.wiswm.nav.support.resources.Colors
import com.wiswm.nav.support.resources.Colors.Companion.NanoWhite
import com.wiswm.nav.support.resources.Strings
import com.wiswm.nav.support.userInterface.viewModel.TaskListViewModel
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_next
import navxsupportapp.composeapp.generated.resources.ic_profile
import navxsupportapp.composeapp.generated.resources.ic_tasks
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

class TaskListScreen() : Tab {
    companion object {
        const val TAG = "TaskListScreen"
    }

    override val options: TabOptions
        @Composable get() {
            return TabOptions(
                index = 0u,
                title = Strings.TASKS,
                icon = painterResource(Res.drawable.ic_tasks)
            )
        }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val taskListViewModel: TaskListViewModel = koinInject()
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
                            title = Strings.TASKS,
                            stompConnectionEvent = taskListViewModel.state.stompConnectionEvent
                        )
                    },
                    content = { padding ->
                        key(taskListViewModel.state.updateUiTrigger) {
                            taskListViewModel.state.taskEntityList?.let {
                                ContentView(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(padding),
                                    taskEntityList = it,
                                    onItemClick = { task ->
                                        if (task.status == TaskEntity.DONE_STATUS) {
                                            taskListViewModel.setUiNotification(
                                                Strings.THE_TASK_ALREADY_IS_DONE
                                            )
                                        } else {
                                            taskListViewModel.setSelectedTaskEntity(task)
                                            when (task.type) {
                                                TaskEntity.TAKE_PHOTO_TYPE -> {
                                                    taskListViewModel.getPhoto()
                                                }

                                                TaskEntity.TAKE_SIGNATURE_TYPE -> {
                                                    taskListViewModel.getSignature()
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                )
                key(taskListViewModel.state.uiNotificationMessage) {
                    LaunchedEffect(Unit) {
                        taskListViewModel.state.uiNotificationMessage?.let {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ContentView(
        modifier: Modifier,
        taskEntityList: List<TaskEntity>,
        onItemClick: (taskEntity: TaskEntity) -> Unit
    ) {
        val listState = rememberLazyListState()

        LaunchedEffect(taskEntityList.size) {
            if (taskEntityList.isNotEmpty()) {
                listState.scrollToItem(taskEntityList.size - 1)
            }
        }

        Box(modifier = modifier.background(Color.White)) {
            LazyColumn(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                state = listState,
            ) {
                items(taskEntityList) { taskEntity ->
                    TaskListItem(taskEntity = taskEntity, onItemClick)
                }
            }
        }
    }

    @Composable
    private fun TopView(title: String, stompConnectionEvent: Stomp.Event) {
        val color = when (stompConnectionEvent) {
            Stomp.Event.UNKNOWN -> Color.Gray
            Stomp.Event.CONNECTED -> Color.Green
            else -> Color.Red
        }

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
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
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
    fun TaskListItem(
        taskEntity: TaskEntity,
        onItemClick: (TaskEntity) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clickable { onItemClick(taskEntity) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Colors.Tusk
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.ORDER + " " + taskEntity.id,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    var backgroundColor = Color.White
                    var contentColor = Color.White
                    var text = ""
                    when (taskEntity.status) {
                        TaskEntity.NEW_STATUS -> {
                            backgroundColor = Colors.BlueRibbon
                            contentColor = Color.White
                            text = Strings.NEW
                        }

                        TaskEntity.PENDING_STATUS -> {
                            backgroundColor = Colors.MarigoldYellow
                            contentColor = Colors.MineShaft
                            text = Strings.PENDING
                        }

                        TaskEntity.DONE_STATUS -> {
                            backgroundColor = Colors.CaribbeanGreen
                            contentColor = Colors.MineShaft
                            text = Strings.DONE
                        }
                    }
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = text,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = backgroundColor,
                            labelColor = contentColor
                        )
                    )
                }

                //**********************************************************************************
                //message from Nav-x without TR-727 tmp fix
                /*
                Text(
                    text = Strings.CUSTOMER + ": " + taskEntity.customerRefId,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                */
                val id = taskEntity.customerRefId.ifEmpty { "ID ${taskEntity.customerId}" }
                Text(
                    text = Strings.CUSTOMER + ": " + id,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                //**********************************************************************************


                if (taskEntity.status != TaskEntity.DONE_STATUS) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        var text = ""
                        when (taskEntity.type) {
                            TaskEntity.TAKE_PHOTO_TYPE -> {
                                text = Strings.PHOTO_REQUIRED
                            }

                            TaskEntity.TAKE_SIGNATURE_TYPE -> {
                                text = Strings.SIGNATURE_REQUIRED
                            }
                        }
                        Icon(
                            painter = painterResource(Res.drawable.ic_profile),
                            tint = Colors.Carnation,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                        Text(
                            text = text,
                            style = Colors.Body1Red,
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            painter = painterResource(Res.drawable.ic_next),
                            tint = Colors.BlueRibbon,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}