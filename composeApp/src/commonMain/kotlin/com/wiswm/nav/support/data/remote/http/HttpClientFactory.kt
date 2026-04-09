package com.wiswm.nav.support.data.remote.http

import io.ktor.client.HttpClient

expect class HttpClientFactory() {
    fun create(): HttpClient
}