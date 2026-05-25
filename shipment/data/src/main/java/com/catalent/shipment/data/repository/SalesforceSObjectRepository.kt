package com.catalent.shipment.data.repository

import com.catalent.core.logging.Loggable
import com.catalent.core.network.ClientProvider
import com.catalent.core.network.RawClientWrapper
import com.catalent.shipment.domain.model.SObjectData
import com.catalent.shipment.domain.repository.SObjectRepository
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager
import com.salesforce.androidsdk.rest.RestClient
import com.salesforce.androidsdk.rest.RestRequest
import com.salesforce.androidsdk.smartstore.store.IndexSpec
import com.salesforce.androidsdk.smartstore.store.QuerySpec
import com.salesforce.androidsdk.smartstore.store.SmartStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class SalesforceSObjectRepository @Inject constructor(
    private val clientProvider: ClientProvider
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
        // Create an internal flow that emits whenever the trigger is pulled
        val internalFlow = refreshTrigger
            .onStart { emit(Unit) } // Emit immediately on start
            .map {
                loadFromSmartStore(sObjectType, displayField, searchQuery)
            }
        
        emitAll(internalFlow)
    }.flowOn(Dispatchers.IO)

    override suspend fun sync(
        sObjectType: String,
        displayField: String
    ) {
        registerSoup(sObjectType, displayField)
        val remoteData = fetchRemote(sObjectType, displayField)
        if (remoteData.isNotEmpty()) {
            upsertToSmartStore(sObjectType, remoteData, displayField)
            // Notify flows that local data has changed
            refreshTrigger.tryEmit(Unit)
        }
    }

    private suspend fun registerSoup(
        sObjectType: String,
        displayField: String,
    ) = withContext(Dispatchers.IO) {
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

    private suspend fun fetchRemote(
        sObjectType: String,
        displayField: String,
    ): List<SObjectData> = withContext(Dispatchers.IO) {
        val clientWrapper = clientProvider.client.value as? RawClientWrapper
        val restClient = clientWrapper?.rawClient as? RestClient ?: return@withContext emptyList()
        
        val query = "SELECT Id, $displayField FROM $sObjectType ORDER BY Id LIMIT 100"
        
        try {
            val request = RestRequest.getRequestForQuery("v60.0", query)
            val response = restClient.sendSync(request)
            if (response.isSuccess) {
                val records = response.asJSONObject().getJSONArray("records")
                buildList {
                    for (i in 0 until records.length()) {
                        val obj = records.getJSONObject(i)
                        add(SObjectData(
                            id = obj.getString("Id"),
                            name = obj.optString(displayField, "No Data"),
                        ))
                    }
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e("fetchRemote", "exception", e)
            emptyList()
        }
    }

    private suspend fun upsertToSmartStore(
        sObjectType: String,
        items: List<SObjectData>,
        displayField: String,
    ) = withContext(Dispatchers.IO) {
        val soupName = getSoupName(sObjectType)
        items.forEach { item ->
            val json = JSONObject().apply {
                put("Id", item.id)
                put(displayField, item.name)
            }
            smartStore.upsert(soupName, json, "Id")
        }
    }

    private fun getSoupName(sObjectType: String) = "${sObjectType}Soup"
}