package com.charlee.sns.activity

import android.app.Application
import com.charlee.sns.manager.SnsEnvController

class SnsApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        val networkParams = NetworkParams()
        SnsEnvController.getInstance().appContext = this
        SnsEnvController.getInstance().networkParams = networkParams
    }
}