package com.wiswm.nav.support.userInterface.screen.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

data class Item(
    val id: Long,
    val value: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Spinner(
    options: List<Item>,
    onOptionSelected: ((option: Item) -> Unit)? = null,
    hint: String? = null,
    selectedOptionIndex: Int = -1,
    enableStatus: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption: Item? = options.getOrNull(selectedOptionIndex)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enableStatus) expanded = !expanded
        },
    ) {
        OutlinedTextField(
            value = selectedOption?.value ?: hint.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { if (selectedOption != null) hint?.let { Text(it) } },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            singleLine = true,
            enabled = enableStatus,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Gray,
                focusedBorderColor = Color.LightGray,
                unfocusedLabelColor = Color.Gray,
                focusedLabelColor = Color.Gray,
                cursorColor = Color.Gray
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.value) },
                    onClick = {
                        selectedOption = option
                        expanded = false
                        onOptionSelected?.let {
                            it(option)
                        }
                    }
                )
            }
        }
    }
}