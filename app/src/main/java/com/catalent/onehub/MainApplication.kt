package com.catalent.onehub

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.catalent.onehub.presentation.ui.MainActivity
import com.catalent.auth.ui.HiddenLoginViewModel
import com.catalent.core.logging.AppLogger
import com.catalent.onehub.sync.SyncWorker
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initLogging()
        initSalesforceSDK()
        schedulePeriodicSync()
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

    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    companion object {
        private const val FEATURE_APP_USES_KOTLIN = "KT"
        private const val SYNC_WORK_NAME = "GlobalPeriodicSync"
    }
}