package com.wiswm.nav.support.data.remote.stomp

import com.wiswm.nav.support.data.local.RepositoryLocal
import com.wiswm.nav.support.util.System

expect class StompManager (repositoryLocal: RepositoryLocal, system: System){
    fun start(stompConnectionDetails: StompConnectionDetails): Boolean
    fun stop(): Boolean
    suspend fun collectStompEvent(onEvent: (Stomp.Event) -> Unit)
    fun getLastStompEvent(): Stomp.Event?
    suspend fun collectStompData(onMessage: suspend (StompMessage) -> Unit)
}