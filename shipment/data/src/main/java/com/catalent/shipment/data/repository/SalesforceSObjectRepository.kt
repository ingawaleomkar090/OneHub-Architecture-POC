package com.catalent.shipment.data.repository

import com.catalent.core.common.AppDispatchers
import com.catalent.core.logging.Loggable
import com.catalent.core.network.ClientProvider
import com.catalent.shipment.domain.model.SObjectData
import com.catalent.shipment.domain.repository.SObjectRepository
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager
import com.salesforce.androidsdk.mobilesync.manager.SyncManager
import com.salesforce.androidsdk.mobilesync.target.SoqlSyncDownTarget
import com.salesforce.androidsdk.mobilesync.util.SyncOptions
import com.salesforce.androidsdk.mobilesync.util.SyncState
import com.salesforce.androidsdk.smartstore.store.IndexSpec
import com.salesforce.androidsdk.smartstore.store.QuerySpec
import com.salesforce.androidsdk.smartstore.store.SmartStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class SalesforceSObjectRepository @Inject constructor(
    private val clientProvider: ClientProvider,
    private val dispatchers: AppDispatchers
) : SObjectRepository, Loggable {

    // A simple trigger to refresh the flow when local data changes
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val account get() =
        SalesforceSDKManager.getInstance().userAccountManager.currentUser

    private val smartStore get() =
        MobileSyncSDKManager.getInstance().getSmartStore(account)

    override fun getSObjects(
        sObjectType: String,
        displayField: String,
        searchQuery: String
    ): Flow<List<SObjectData>> = flow {
        val internalFlow = refreshTrigger
            .onStart { emit(Unit) }
            .map {
                d("getSObjects","displayField--> $displayField \t  searchQuery--> $searchQuery")
                loadFromSmartStore(sObjectType, displayField, searchQuery)
            }
        
        emitAll(internalFlow)
    }.flowOn(dispatchers.io)

    override suspend fun sync(
        sObjectType: String,
        displayField: String
    ) = withContext(dispatchers.io) {
        registerSoup(sObjectType, displayField)
        
        val syncManager = SyncManager.getInstance(account)
        val soupName = getSoupName(sObjectType)
        
        val target = SoqlSyncDownTarget("SELECT Id, $displayField FROM $sObjectType ORDER BY Id LIMIT 100")
        val options = SyncOptions.optionsForSyncDown(SyncState.MergeMode.OVERWRITE)
        
        suspendCancellableCoroutine { continuation ->
            syncManager.syncDown(target, options, soupName, object : SyncManager.SyncUpdateCallback {
                override fun onUpdate(syncState: SyncState) {
                    if (syncState.isDone) {
                        d("sync", "syncDown completed for-->  $sObjectType")
                        refreshTrigger.tryEmit(Unit)
                        if (continuation.isActive) continuation.resume(Unit)
                    } else if (syncState.status == SyncState.Status.FAILED) {
                        e("sync", "syncDown failed for $sObjectType")
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                }
            })
        }
    }

    private suspend fun registerSoup(
        sObjectType: String,
        displayField: String,
    ) = withContext(dispatchers.io) {
        val soupName = getSoupName(sObjectType)
        if (!smartStore.hasSoup(soupName)) {
            val indexSpecs = arrayOf(
                IndexSpec("Id", SmartStore.Type.string),
                IndexSpec(displayField, SmartStore.Type.string),
            )
            smartStore.registerSoup(soupName, indexSpecs)
            d("registerSoup", "registered $soupName")
        }
    }

    private fun loadFromSmartStore(
        sObjectType: String,
        displayField: String,
        searchQuery: String,
    ): List<SObjectData> {
        val soupName = getSoupName(sObjectType)
        if (!smartStore.hasSoup(soupName)) return emptyList()

        val querySpec = if (searchQuery.isEmpty()) {
            QuerySpec.buildAllQuerySpec(soupName, "Id", QuerySpec.Order.ascending, 100)
        } else {
            QuerySpec.buildLikeQuerySpec(
                soupName, displayField, "%$searchQuery%",
                "Id", QuerySpec.Order.ascending, 100,
            )
        }
        return try {
            val results = smartStore.query(querySpec, 0)
            buildList {
                for (i in 0 until results.length()) {
                    val obj = results.getJSONObject(i)
                    add(SObjectData(
                        id = obj.getString("Id"),
                        name = obj.optString(displayField, "No Data"),
                    ))
                }
            }
        } catch (e: Exception) {
            e("loadFromSmartStore", "query failed", e)
            emptyList()
        }
    }

    private fun getSoupName(sObjectType: String) = "${sObjectType}Soup"
}