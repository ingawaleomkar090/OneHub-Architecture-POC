package com.catalent.onehub.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.catalent.auth.domain.AuthRepository
import com.catalent.auth.domain.AuthState
import com.catalent.shipment.domain.repository.SObjectRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val shipmentRepository: SObjectRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 1. Check if user is still authenticated
        val authState = authRepository.authState.first()
        if (authState !is AuthState.Authenticated) {
            return Result.failure()
        }

        return try {
            // 2. Perform sync for each critical feature
            shipmentRepository.sync("Contact", "Name")
            shipmentRepository.sync("Account", "Name")
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}