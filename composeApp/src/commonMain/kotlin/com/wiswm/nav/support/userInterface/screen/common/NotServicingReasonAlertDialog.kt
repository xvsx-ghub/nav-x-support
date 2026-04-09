package com.wiswm.nav.support.userInterface.screen.common
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wiswm.nav.support.data.local.dataBase.entity.NotServicingReasonEntity
import com.wiswm.nav.support.resources.Colors.Companion.BlueRibbon
import com.wiswm.nav.support.resources.Colors.Companion.WhiteLiliac
import com.wiswm.nav.support.resources.Strings

@Composable
fun NotServicingReasonAlertDialog(
    notServicingReasonList: List<NotServicingReasonEntity>?,
    visibilityStatus: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (notServicingReasonEntity: NotServicingReasonEntity) -> Unit,
    onError: (message: String) -> Unit
) {
    if(visibilityStatus) {
        notServicingReasonList?.let { nnNotServicingReasonList ->

            var selectedOption by remember {
                mutableStateOf(nnNotServicingReasonList.firstOrNull())
            }

            if (nnNotServicingReasonList.isEmpty() || selectedOption == null) {
                onDismiss()
                return
            }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = "Select the reason",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        nnNotServicingReasonList.forEach { notServicingReason ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = (notServicingReason == selectedOption),
                                    onClick = { selectedOption = notServicingReason },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = BlueRibbon
                                    )
                                )
                                Text(
                                    text = notServicingReason.description,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedOption?.let { onConfirm(it) }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(WhiteLiliac)
                    ) {
                        Text(text = Strings.CONFIRM, color = BlueRibbon)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(WhiteLiliac)
                    ) {
                        Text(text = Strings.CANCEL, color = BlueRibbon)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            return
        }
        onError(Strings.NOT_SERVICING_REASON_LIST_IS_EMPTY)
    }
}