package com.sipedas.app

import android.app.Application
import com.sipedas.app.data.local.SessionStore

class SipedasApp : Application() {
    lateinit var sessionStore: SessionStore
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = SessionStore(this)
    }
}