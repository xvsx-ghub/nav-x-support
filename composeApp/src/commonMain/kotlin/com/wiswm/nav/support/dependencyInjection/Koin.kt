package com.wiswm.nav.support.dependencyInjection

import org.koin.core.context.startKoin
import org.koin.core.error.KoinApplicationAlreadyStartedException

fun initKoin() {
    try {
        startKoin {
            modules(baseApplicationModule)
        }
    } catch (_: KoinApplicationAlreadyStartedException) {
        // iOS can recreate UI controllers and call init more than once.
    }
}