package com.wiswm.nav.support

import androidx.compose.runtime.Composable

@Composable
actual fun Permission(
    content: @Composable () -> Unit
) {
    content()
}