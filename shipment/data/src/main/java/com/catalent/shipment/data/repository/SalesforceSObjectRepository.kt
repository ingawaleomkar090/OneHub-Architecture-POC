package com.catalent.shipment.data.repository

import com.catalent.core.common.AppDispatchers
import com.catalent.core.logging.Loggable
import com.catalent.shipment.domain.model.SObjectData
import com.catalent.shipment.domain.repository.SObjectRepository
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager
import com.salesforce.androidsdk.mobilesync.manager.SyncManager
import com.salesforce.androidsdk.mobilesync.target.SoqlSyncDownTarget
import com.salesforce.androidsdk.mobilesync.util.SyncOptions
import com.salesforce.androidsdk.mobilesync.util.SyncState
import com.salesforce.androidsdk.rest.RestRequest
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Singleton
class SalesforceSObjectRepository @Inject constructor(
    private val dispatchers: AppDispatchers
) : SObjectRepository, Loggable {

    // A simple trigger to refresh the flow when local data changes
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val account get() =
        SalesforceSDKManager.getInstance().userAccountManager.currentUser

    private val smartStore: SmartStore get() =
        MobileSyncSDKManager.getInstance().getSmartStore(account)

    override fun getSObjects(
        sObjectType: String,
        displayField: String,
        searchQuery: String
    ): Flow<List<SObjectData>> = flow {
        // Emit cached data immediately if available
        emit(loadFromSmartStore(sObjectType, displayField, searchQuery))

        // Then listen for updates
        val internalFlow = refreshTrigger
            .map {
                loadFromSmartStore(sObjectType, displayField, searchQuery)
            }
        
        emitAll(internalFlow)
    }.flowOn(dispatchers.io)

    override suspend fun sync(
        sObjectType: String,
        displayField: String
    ) = withContext(dispatchers.io) {
        val currentAccount = account ?: return@withContext
        val store = MobileSyncSDKManager.getInstance().getSmartStore(currentAccount)
        registerSoup(store, sObjectType, displayField)
        
        val syncManager = SyncManager.getInstance(currentAccount)
        val soupName = getSoupName(sObjectType)
        
        val target = SoqlSyncDownTarget("SELECT Id, $displayField FROM $sObjectType ORDER BY Id LIMIT 20")
        val options = SyncOptions.optionsForSyncDown(SyncState.MergeMode.OVERWRITE)
        
        suspendCancellableCoroutine { continuation ->
            syncManager.syncDown(target, options, soupName, object : SyncManager.SyncUpdateCallback {
                override fun onUpdate(sync: SyncState) {
                    when (sync.status) {
                        SyncState.Status.DONE -> {
                            refreshTrigger.tryEmit(Unit)
                            if (continuation.isActive) continuation.resume(Unit)
                        }
                        SyncState.Status.FAILED -> {
                            e("sync", "Sync failed for $sObjectType: ${sync.progress}")
                            if (continuation.isActive) continuation.resume(Unit)
                        }
                        else -> { /* no-op */ }
                    }
                }
            })
        }
    }

    override suspend fun searchRemotely(
        sObjectType: String,
        displayField: String,
        query: String
    ) = withContext(dispatchers.io) {
        val restClient = SalesforceSDKManager.getInstance().clientManager.peekRestClient() ?: return@withContext
        
        val soql = "SELECT Id, $displayField FROM $sObjectType WHERE $displayField LIKE '%$query%' ORDER BY Id LIMIT 20"
        val request = RestRequest.getRequestForQuery("v60.0", soql)
        
        try {
            val response = restClient.sendSync(request)
            if (response.isSuccess) {
                val records = response.asJSONObject().getJSONArray("records")
                val soupName = getSoupName(sObjectType)
                val store = smartStore
                
                for (i in 0 until records.length()) {
                    store.upsert(soupName, records.getJSONObject(i), "Id")
                }
                refreshTrigger.tryEmit(Unit)
            }
        } catch (e: Exception) {
            e("searchRemotely", "Search failed", e)
        }
    }

    override suspend fun loadMore(
        sObjectType: String,
        displayField: String,
        offset: Int
    ) = withContext(dispatchers.io) {
        val restClient = SalesforceSDKManager.getInstance().clientManager.peekRestClient() ?: return@withContext
        
        val soql = "SELECT Id, $displayField FROM $sObjectType ORDER BY Id LIMIT 20 OFFSET $offset"
        val request = RestRequest.getRequestForQuery("v60.0", soql)
        
        try {
            val response = restClient.sendSync(request)
            if (response.isSuccess) {
                val records = response.asJSONObject().getJSONArray("records")
                val soupName = getSoupName(sObjectType)
                val store = smartStore
                
                for (i in 0 until records.length()) {
                    store.upsert(soupName, records.getJSONObject(i), "Id")
                }
                refreshTrigger.tryEmit(Unit)
            }
        } catch (e: Exception) {
            e("loadMore", "Load more failed", e)
        }
    }

    private fun registerSoup(
        store: SmartStore,
        sObjectType: String,
        displayField: String,
    ) {
        val soupName = getSoupName(sObjectType)
        val indexSpecs = arrayOf(
            IndexSpec("Id", SmartStore.Type.string),
            IndexSpec(displayField, SmartStore.Type.string),
        )
        // Always call registerSoup to ensure metadata is in sync with the desired schema.
        // SmartStore handles re-indexing if the IndexSpec has changed.
        store.registerSoup(soupName, indexSpecs)
    }

    private fun loadFromSmartStore(
        sObjectType: String,
        displayField: String,
        searchQuery: String,
    ): List<SObjectData> {
        val store = smartStore
        val soupName = getSoupName(sObjectType)
        if (!store.hasSoup(soupName)) return emptyList()

        val querySpec = if (searchQuery.isEmpty()) {
            QuerySpec.buildAllQuerySpec(soupName, "Id", QuerySpec.Order.ascending, 20)
        } else {
            // Use buildLikeQuerySpec for partial matching in string fields
            QuerySpec.buildLikeQuerySpec(
                soupName, displayField, "%$searchQuery%",
                "Id", QuerySpec.Order.ascending, 20,
            )
        }
        return try {
            val results = store.query(querySpec, 0)
            buildList {
                for (i in 0 until results.length()) {
                    val obj = results.getJSONObject(i)
                    add(SObjectData(
                        id = obj.getString("Id"),
                        name = obj.optString(displayField, "No Data"),
                    ))
                }
            }
        } catch (exception: Exception) {
            e("loadFromSmartStore", "query failed", exception)
            emptyList()
        }
    }

    private fun getSoupName(sObjectType: String) = "${sObjectType}Soup"
}