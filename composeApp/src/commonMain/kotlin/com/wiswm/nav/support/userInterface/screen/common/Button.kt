package com.wiswm.nav.support.userInterface.screen.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wiswm.nav.support.resources.Colors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class ButtonType {
    Unknown,
    White
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().height(48.dp),
    text: String? = null,
    image: DrawableResource? = null,
    tint: Color = Color.Unspecified,
    enableStatus: Boolean = true,
    lazyStatus: Boolean = false,
    type: ButtonType = ButtonType.Unknown
) {
    var pressed by remember { mutableStateOf(false) }

    val backgroundGradient = when (type) {
        ButtonType.White -> Brush.horizontalGradient(
            listOf(Colors.White, Colors.White)
        )

        else -> {
            if(lazyStatus){
                Brush.horizontalGradient(
                    listOf(Colors.LightGrey, Colors.LightGrey)
                )
            }else{
                when {
                    !enableStatus -> Brush.horizontalGradient(
                        listOf(Colors.LightGrey, Colors.LightGrey)
                    )

                    pressed -> Brush.horizontalGradient(
                        listOf(Colors.VividBlue, Colors.SaturatedBlue)
                    )

                    else -> Brush.horizontalGradient(
                        listOf(Colors.SaturatedBlue, Colors.VividBlue)
                    )
                }
            }
        }
    }

    val textColor = when (type) {
        ButtonType.White -> Colors.SaturatedBlue
        else -> Colors.White
    }

    Row(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                clip = false
            )
            .background(
                brush = backgroundGradient,
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(enableStatus) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        if (enableStatus) onClick()
                    }
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        image?.let {
            Icon(
                painter = painterResource(it),
                contentDescription = null,
                tint = tint,
            )
        }

        if(image != null && text != null) Spacer(Modifier.size(8.dp))

        text?.let {
            Box(
                modifier = Modifier.height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = it,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    Button(
        onClick = {},
        text = "Text"
    )
}