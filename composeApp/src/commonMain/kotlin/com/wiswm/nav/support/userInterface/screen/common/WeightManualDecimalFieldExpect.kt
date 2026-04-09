package com.wiswm.nav.support.userInterface.screen.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal fun filterManualWeightDecimal(raw: String): String? {
    val filtered = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
    return if (filtered.count { it == '.' } <= 1) filtered else null
}

@Composable
expect fun WeightManualDecimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    units: String,
    modifier: Modifier = Modifier,
)
