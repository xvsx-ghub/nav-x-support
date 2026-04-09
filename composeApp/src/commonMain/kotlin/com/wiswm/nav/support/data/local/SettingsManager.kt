package com.wiswm.nav.support.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SettingsManager(
    private val settings: Settings
) {
    companion object {
        private const val WIS_URL_KEY = "wisUrl"
        private const val WIS_NAME_KEY = "wisName"
        private const val SESSION_KEY_KEY = "sessionKey"
        private const val ROSTER_ID_KEY = "rosterId"
        private const val TRANSACTION_ID = "transactionId"
    }

    var wisUrl: String
        get() = settings.getString(WIS_URL_KEY, "")
        set(value) = settings.set(WIS_URL_KEY, value)

    var wisName: String
        get() = settings.getString(WIS_NAME_KEY, "")
        set(value) = settings.set(WIS_NAME_KEY, value)

    var sessionKey: String
        get() = settings.getString(SESSION_KEY_KEY, "")
        set(value) = settings.set(SESSION_KEY_KEY, value)

    var rosterId: String
        get() = settings.getString(ROSTER_ID_KEY, "")
        set(value) = settings.set(ROSTER_ID_KEY, value)

    val transactionId: Int
        get(){
            val id = settings.getInt(TRANSACTION_ID, Int.MAX_VALUE)
            if(id < Int.MAX_VALUE / 2) settings.set(TRANSACTION_ID, Int.MAX_VALUE)
            else settings.set(TRANSACTION_ID, id - 1)
            return id
        }

    fun clearAll() {
        settings.clear()
    }
}