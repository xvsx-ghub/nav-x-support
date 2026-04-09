package com.wiswm.nav.support.util


expect class ConnectivityObserver(){
     fun create(onConnectionStateChanged: (onlineStatus: Boolean)-> Unit)
}