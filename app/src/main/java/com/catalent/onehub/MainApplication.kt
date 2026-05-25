package com.catalent.onehub

import android.app.Application
import com.catalent.onehub.presentation.ui.MainActivity
import com.catalent.auth.ui.HiddenLoginViewModel
import com.catalent.core.logging.AppLogger
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initLogging()
        initSalesforceSDK()
    }

    private fun initLogging() {
        @Suppress("KotlinConstantConditions")
        AppLogger.init(this, BuildConfig.ENABLE_CONSOLE_LOGGING)
    }

    private fun initSalesforceSDK() {
        MobileSyncSDKManager.initNative(applicationContext, MainActivity::class.java, null)
        MobileSyncSDKManager.getInstance().registerUsedAppFeature(FEATURE_APP_USES_KOTLIN)
        MobileSyncSDKManager.getInstance().loginViewModelFactory = HiddenLoginViewModel.Factory
    }

    companion object {
        private const val FEATURE_APP_USES_KOTLIN = "KT"
    }
}