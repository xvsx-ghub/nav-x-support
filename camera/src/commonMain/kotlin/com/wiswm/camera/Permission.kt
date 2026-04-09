package com.wiswm.camera

import androidx.compose.runtime.Composable

@Composable
expect fun Permission(
    content: @Composable () -> Unit = {}
)