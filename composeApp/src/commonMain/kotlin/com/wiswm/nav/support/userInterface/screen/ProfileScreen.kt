package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.wiswm.nav.support.Permission
import com.wiswm.nav.support.resources.Colors
import com.wiswm.nav.support.resources.Colors.Companion.LimedSpruce
import com.wiswm.nav.support.resources.Colors.Companion.NanoWhite
import com.wiswm.nav.support.resources.Strings
import com.wiswm.nav.support.userInterface.screen.common.MulticolorProgressBar
import com.wiswm.nav.support.userInterface.viewModel.ProfileViewModel
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_logout
import navxsupportapp.composeapp.generated.resources.ic_profile
import navxsupportapp.composeapp.generated.resources.ic_user_photo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

class ProfileScreen() : Tab {
    companion object {
        const val TAG = "ProfileScreen"
    }

    override val options: TabOptions
        @Composable get() {
            return TabOptions(
                index = 3u,
                title = Strings.PROFILE,
                icon = painterResource(Res.drawable.ic_profile)
            )
        }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val profileViewModel: ProfileViewModel = koinInject()
        Permission {
            Box(modifier = Modifier.background(Colors.Black)) {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = Colors.PacificBlue,
                    topBar = {
                        TopView(Strings.PROFILE)
                    },
                    content = { padding ->
                        ContentView(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            userName = profileViewModel.state.authorizationEntity?.name ?: "",
                            companyName = profileViewModel.state.wisName ?: ""
                        )
                    },
                    bottomBar = {
                        BottomView(onClickLogout = {
                            profileViewModel.logout()
                        })
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                )
                MulticolorProgressBar(visibilityStatus = profileViewModel.state.progressBarVisibilityStatus)
                key(profileViewModel.state.uiNotificationMessage) {
                    LaunchedEffect(Unit) {
                        profileViewModel.state.uiNotificationMessage?.let {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TopView(title: String) {
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
            }
        }
    }

    @Composable
    private fun ContentView(
        modifier: Modifier,
        userName: String,
        companyName: String
    ) {
        val scrollState = rememberScrollState()
        Box(modifier = modifier) {
            Column(
                modifier = Modifier
                    .background(Colors.White)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
            ) {
                UserInfo(
                    userName = userName,
                    companyName = companyName
                )
                HorizontalDivider(Modifier, 1.dp, LimedSpruce)
            }
        }
    }

    @Composable
    private fun BottomView(onClickLogout: ()-> Unit) {
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
                    onClickLogout()
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_logout),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = Strings.LOGOUT, color = Color.White)
            }
        }
    }

    @Composable
    fun UserInfo(userName: String, companyName: String) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_user_photo),
                tint = LimedSpruce,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column{
                Text(
                    text = Strings.NAME,
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = Strings.COMPANY,
                    style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = userName,
                    style = typography.bodyLarge
                )
                Text(
                    text = companyName,
                    style = typography.bodyLarge
                )
            }
        }
    }
}