package com.wiswm.camera

import androidx.compose.runtime.Composable

@Composable
actual fun Permission(
    content: @Composable () -> Unit
) {
    content()
}