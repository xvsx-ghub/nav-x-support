package com.wiswm.nav.support

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform