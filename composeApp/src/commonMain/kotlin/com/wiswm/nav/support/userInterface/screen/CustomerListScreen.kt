package com.wiswm.nav.support.userInterface.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.wiswm.nav.support.Permission
import com.wiswm.nav.support.resources.Colors
import com.wiswm.nav.support.resources.Colors.Companion.BlueRibbon
import com.wiswm.nav.support.resources.Strings
import com.wiswm.nav.support.userInterface.viewModel.CustomerListViewModel
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_customers
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.wiswm.nav.support.resources.Colors.Companion.Body1Red
import com.wiswm.nav.support.resources.Colors.Companion.Carnation
import com.wiswm.nav.support.resources.Colors.Companion.NanoWhite
import com.wiswm.nav.support.resources.Colors.Companion.WhiteLiliac
import navxsupportapp.composeapp.generated.resources.ic_attention_triangle
import navxsupportapp.composeapp.generated.resources.ic_gate
import navxsupportapp.composeapp.generated.resources.ic_location
import navxsupportapp.composeapp.generated.resources.ic_next
import navxsupportapp.composeapp.generated.resources.ic_problem
import navxsupportapp.composeapp.generated.resources.ic_qr_code
import navxsupportapp.composeapp.generated.resources.ic_route
import navxsupportapp.composeapp.generated.resources.ic_sign
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextAlign
import com.wiswm.nav.support.data.local.dataBase.entity.CustomerEntity
import com.wiswm.nav.support.resources.Colors.Companion.LimedSpruce
import com.wiswm.nav.support.userInterface.screen.common.MulticolorProgressBar
import com.wiswm.nav.support.userInterface.screen.common.NotServicingReasonAlertDialog
import navxsupportapp.composeapp.generated.resources.ic_clear
import navxsupportapp.composeapp.generated.resources.ic_question
import navxsupportapp.composeapp.generated.resources.ic_refresh
import navxsupportapp.composeapp.generated.resources.ic_search
import org.koin.compose.koinInject

class CustomerListScreen() : Tab {
    companion object {
        const val TAG = "CustomerListScreen"
    }

    override val options: TabOptions
        @Composable get() {
            return TabOptions(
                index = 1u,
                title = Strings.CUSTOMERS,
                icon = painterResource(Res.drawable.ic_customers)
            )
        }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val customerListViewModel: CustomerListViewModel = koinInject()
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
                            title = Strings.CUSTOMERS,
                            onRefreshClick = {
                                customerListViewModel.getCustomerList()
                            }
                        )
                    },
                    content = { padding ->
                        ContentView(
                            modifier = Modifier.padding(padding),
                            customerListViewModel = customerListViewModel
                        )
                    },
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                )

                MulticolorProgressBar(
                    modifier = Modifier.fillMaxSize(),
                    visibilityStatus = customerListViewModel.state.progressBarVisibilityStatus
                )

                key(customerListViewModel.state.notServicingReasonAlertDialogVisibilityStatus) {
                    NotServicingReasonAlertDialog(
                        notServicingReasonList = customerListViewModel
                            .state.notServicingReasonEntityList,
                        visibilityStatus = customerListViewModel
                            .state.notServicingReasonAlertDialogVisibilityStatus,
                        onDismiss = {
                            customerListViewModel.pushNotServicingReasonAlertDialog(false)
                        },
                        onConfirm = { notServicingReasonEntity ->
                            customerListViewModel.pushNotServicingReasonAlertDialog(false)
                            customerListViewModel.createNotServicingReasonPhotoTask(
                                notServicingReasonEntity
                            )
                        },
                        onError = { message ->
                            customerListViewModel.setUiNotification(message)
                            customerListViewModel.pushNotServicingReasonAlertDialog(false)
                        }
                    )
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
    private fun ContentView(modifier: Modifier, customerListViewModel: CustomerListViewModel) {
        if (customerListViewModel.state.customerEntityList.isNullOrEmpty()
            && !customerListViewModel.state.searchActiveStatus
        ) EmptyRouteNotification()
        else CustomerList(
            modifier = modifier,
            customerListViewModel = customerListViewModel
        )
    }

    @Composable
    private fun EmptyRouteNotification() {
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .background(Color.White)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_question),
                    contentDescription = null,
                    tint = LimedSpruce,
                    modifier = Modifier
                        .background(NanoWhite)
                )
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = Strings.NO_ROUTE_SELECTED,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    textAlign = TextAlign.Center,
                    text = Strings.PLEASE_SELECT_A_ROUTE,
                )
            }
        }
    }

    @Composable
    private fun CustomerList(modifier: Modifier, customerListViewModel: CustomerListViewModel) {
        val snackbarHostState = remember { SnackbarHostState() }
        var searchBarText by remember { mutableStateOf("") }
        val customerListState =
            rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

        customerListViewModel.updateCustomerList(searchBarText)

        Box(
            modifier = modifier
                .background(Color.White)
                .fillMaxSize()
        ) {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                containerColor = Colors.White
            ) { paddingValues ->
                customerListViewModel.state.customerEntityList?.let { customerListEntity ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        state = customerListState,
                    ) {
                        item {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                RouteTextField(
                                    routeName = "${Strings.ROUTED_ON_} ${customerListViewModel.state.routeName}"
                                )
                                Spacer(modifier = Modifier.size(16.dp))
                                SearchBar(
                                    onTextChange = { text ->
                                        searchBarText = text
                                        customerListViewModel.updateCustomerList(text)
                                    },
                                    onSearchButtonClick = { text ->
                                        customerListViewModel.updateCustomerList(text)
                                    },
                                    onScanButtonClick = {
                                        customerListViewModel.getQrCode()
                                    },
                                    onClearButtonClick = {
                                        customerListViewModel.setQrCode(null)
                                        searchBarText = ""
                                        customerListViewModel.updateCustomerList("")
                                    },
                                    data = searchBarText,
                                )
                            }
                        }
                        items(customerListEntity) { customer ->
                            CustomerListItem(
                                customer = customer,
                                onClick = {
                                    customerListViewModel.setSelectedCustomerEntity(customer)
                                    customerListViewModel.showCustomerDetails()
                                },
                                onClickSign = {
                                    customerListViewModel.setSelectedCustomerEntity(customer)
                                    customerListViewModel.createSignatureTask()
                                },
                                onClickReport = {
                                    customerListViewModel.setSelectedCustomerEntity(customer)
                                    customerListViewModel.pushNotServicingReasonAlertDialog(true)
                                }
                            )
                        }
                    }
                }
            }

            key(customerListViewModel.state.qrCode) {
                customerListViewModel.state.qrCode?.let {
                    searchBarText = it
                    customerListViewModel.updateCustomerList(it)
                }
            }
        }

        key(customerListViewModel.state.uiNotificationMessage) {
            LaunchedEffect(Unit) {
                customerListViewModel.state.uiNotificationMessage?.let {
                    snackbarHostState.showSnackbar(it)
                }
            }
        }
    }

    @Composable
    fun RouteTextField(
        routeName: String,
        modifier: Modifier = Modifier
    ) {
        val shape = RoundedCornerShape(30.dp)
        val backgroundColor = Colors.SoftWhite
        val borderColor = Colors.Platinum

        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor, shape)
                .border(1.dp, borderColor, shape),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.size(64.dp),
                enabled = false,
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_route),
                    contentDescription = null,
                    tint = Color.Black,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = routeName,
                onValueChange = {},
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                enabled = false,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }

    @Composable
    fun SearchBar(
        onTextChange: (String) -> Unit,
        onSearchButtonClick: (String) -> Unit,
        onScanButtonClick: () -> Unit,
        onClearButtonClick: () -> Unit,
        data: String
    ) {
        Box(modifier = Modifier.background(Colors.PacificBlue)) {
            val shape = RoundedCornerShape(4.dp)
            val backgroundColor = Color.White
            val borderColor = Color.Gray

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor, shape)
                    .border(1.dp, borderColor, shape),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.size(64.dp),
                    onClick = { onSearchButtonClick(data) }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = null,
                        tint = Color.Black,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                BasicTextField(
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    value = data,
                    onValueChange = { onTextChange(it) },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        color = Color.Black,
                    ),
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (data.isEmpty()) {
                    IconButton(
                        modifier = Modifier.size(64.dp),
                        onClick = { onScanButtonClick() }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_qr_code),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    }
                } else {
                    IconButton(
                        modifier = Modifier.size(64.dp).padding(8.dp),
                        onClick = { onClearButtonClick() }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_clear),
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun CustomerListItem(
        customer: CustomerEntity,
        onClick: (customer: CustomerEntity) -> Unit,
        onClickSign: (customer: CustomerEntity) -> Unit,
        onClickReport: (customer: CustomerEntity) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .clickable { onClick(customer) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NanoWhite)
        ) {
            CustomerListItemContent(customer = customer, onClickSign, onClickReport)
        }
    }

    @Composable
    fun CustomerListItemContent(
        customer: CustomerEntity,
        onClickSign: (customer: CustomerEntity) -> Unit,
        onClickReport: (customer: CustomerEntity) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            CustomerListItemLine1(customer)
            CustomerListItemLine2(customer)
            if (customer.gateCode.isNotEmpty()) {
                CustomerListItemLine3(customer)
            }
            if (customer.notes.isNotEmpty()) {
                CustomerListItemLine4(customer)
            }
            if (customer.signatureRequired == 1) {
                CustomerListItemLine5(customer, onClickSign)
            }
            CustomerListItemLine6(customer, onClickReport)
        }
    }

    @Composable
    fun CustomerListItemLine1(customer: CustomerEntity) {
        Row(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = Strings.CUSTOMER_ID + customer.customerRefId,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.weight(1f))

            Icon(
                painter = painterResource(Res.drawable.ic_next),
                tint = BlueRibbon,
                contentDescription = null
            )
        }
    }

    @Composable
    fun CustomerListItemLine2(customer: CustomerEntity) {
        Row(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_location),
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = customer.address,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }

    @Composable
    fun CustomerListItemLine3(customer: CustomerEntity) {
        Row(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_gate),
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = customer.gateCode,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }

    @Composable
    fun CustomerListItemLine4(customer: CustomerEntity) {
        Row(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = Strings.NOTES_,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = customer.notes,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }

    @Composable
    fun CustomerListItemLine5(
        customer: CustomerEntity,
        onClickSign: (customer: CustomerEntity) -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_attention_triangle),
                tint = Carnation,
                contentDescription = null
            )

            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = Strings.SIGNATURE_REQUIRED,
                style = Body1Red
            )

            Spacer(Modifier.weight(1f))

            Button(
                modifier = Modifier
                    .wrapContentSize(),
                colors = ButtonDefaults.buttonColors(containerColor = WhiteLiliac),
                onClick = { onClickSign(customer) }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_sign),
                    contentDescription = null,
                    tint = BlueRibbon,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(text = Strings.SIGN, color = BlueRibbon)
            }
        }
    }

    @Composable
    fun CustomerListItemLine6(
        customer: CustomerEntity,
        onClickReport: (customer: CustomerEntity) -> Unit
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Carnation),
            onClick = { onClickReport(customer) }
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_problem),
                contentDescription = null,
                tint = WhiteLiliac,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text = Strings.REPORT_A_PROBLEM, color = WhiteLiliac)
        }
    }
}