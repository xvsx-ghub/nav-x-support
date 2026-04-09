package com.wiswm.nav.support.userInterface.screen.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wiswm.nav.support.resources.Strings

@Composable
fun TwoButtonsDialog(
    text: AnnotatedString,
    visibilityStatus: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    title: String? = null
) {
    if (visibilityStatus) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    Modifier.background(Color.White).padding(16.dp)
                ) {
                    title?.let{
                        Text(
                            text = it,
                            modifier = Modifier.padding(8.dp).fillMaxWidth(),
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.size(8.dp))
                    }
                    Text(
                        text = text,
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.size(16.dp))
                    Row {
                        Button(
                            onClick =  onCancel,
                            modifier = Modifier
                                .weight(1f),
                            text = Strings.CANCEL,
                            type = ButtonType.White
                        )
                        Spacer(Modifier.size(32.dp))
                        Button(
                            onClick =  onConfirm,
                            modifier = Modifier
                                .weight(1f),
                            text = Strings.CONFIRM,
                        )
                    }
                }
            }
        }
    }
}