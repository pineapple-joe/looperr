package com.looperr.app

import android.app.Application

class LooperrApp : Application() {

    companion object {
        lateinit var instance: LooperrApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
