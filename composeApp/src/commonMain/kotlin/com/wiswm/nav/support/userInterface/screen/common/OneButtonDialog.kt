package com.wiswm.nav.support.userInterface.screen.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun OneButtonDialog(
    visibilityStatus: Boolean,
    onClick: () -> Unit,
    buttonText: String?,
    icon: DrawableResource? = null,
    title: String? = null,
    text: String? = null
) {
    if (visibilityStatus) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    icon?.let {
                        Icon(
                            modifier = Modifier
                                .padding(8.dp),
                            tint = Color.Unspecified,
                            painter = painterResource(it), //Res.drawable.ic_report_completed
                            contentDescription = null,
                        )
                    }

                    title?.let {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = it,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    text?.let {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = it,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = onClick,
                        text = buttonText ?: "",
                    )
                }
            }
        }
    }
}