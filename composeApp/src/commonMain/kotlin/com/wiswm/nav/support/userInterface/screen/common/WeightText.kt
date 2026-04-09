package com.wiswm.nav.support.userInterface.screen.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wiswm.nav.support.resources.Strings
import navxsupportapp.composeapp.generated.resources.Res
import navxsupportapp.composeapp.generated.resources.ic_refresh

enum class WeightTextType {
    Unknown,
    AutomaticIn,
    AutomaticOut,
    AutomaticNet,
    ManualIn,
    ManualOut,
    ManualNet,
}

@Composable
private fun ManualWeightColumn(
    label: String,
    value: String,
    units: String,
    onValueChanged: ((String) -> Unit)?,
) {
    var text by remember { mutableStateOf(value) }
    Column {
        Text(text = label)
        Spacer(Modifier.size(4.dp))
        WeightManualDecimalTextField(
            value = text,
            onValueChange = { filtered ->
                text = filtered
                onValueChanged?.invoke(filtered)
            },
            units = units,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun WeightText(
    value: String = "",
    units: String = "kg",
    onValueChanged: ((value: String) -> Unit)? = null,
    type: WeightTextType = WeightTextType.Unknown,
    onRefreshClick: (() -> Unit)? = null,
    lazyStatus: Boolean = true
) {
    when (type) {
        WeightTextType.AutomaticIn -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = Strings.WEIGHT_IN_)
                Spacer(Modifier.size(16.dp))
                Text(text = "$value $units", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { onRefreshClick?.invoke() },
                    image = Res.drawable.ic_refresh,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White,
                    lazyStatus = lazyStatus
                )
            }
        }

        WeightTextType.AutomaticOut -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = Strings.WEIGHT_OUT_)
                Spacer(Modifier.size(16.dp))
                Text(text = "$value $units", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { onRefreshClick?.invoke() },
                    image = Res.drawable.ic_refresh,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White,
                    lazyStatus = lazyStatus
                )
            }
        }

        WeightTextType.AutomaticNet -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = Strings.NET_WEIGHT_)
                Spacer(Modifier.size(16.dp))
                Text(text = "$value $units", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { onRefreshClick?.invoke() },
                    image = Res.drawable.ic_refresh,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White,
                    lazyStatus = lazyStatus
                )
            }
        }

        WeightTextType.ManualIn -> ManualWeightColumn(
            label = Strings.WEIGHT_IN_,
            value = value,
            units = units,
            onValueChanged = onValueChanged,
        )

        WeightTextType.ManualOut -> ManualWeightColumn(
            label = Strings.WEIGHT_OUT_,
            value = value,
            units = units,
            onValueChanged = onValueChanged,
        )

        WeightTextType.ManualNet, WeightTextType.Unknown -> ManualWeightColumn(
            label = Strings.NET_WEIGHT_,
            value = value,
            units = units,
            onValueChanged = onValueChanged,
        )
    }
}
