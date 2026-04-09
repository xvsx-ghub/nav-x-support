package com.wiswm.nav.support

import androidx.compose.runtime.Composable

@Composable
expect fun Permission(
    content: @Composable () -> Unit = {}
)