package com.catalent.shipment.data.repository

import com.catalent.core.logging.Loggable
import com.catalent.core.network.ClientProvider
import com.salesforce.androidsdk.app.SalesforceSDKManager
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager
import com.salesforce.androidsdk.rest.RestRequest
import com.salesforce.androidsdk.smartstore.store.IndexSpec
import com.salesforce.androidsdk.smartstore.store.QuerySpec
import com.salesforce.androidsdk.smartstore.store.SmartStore
import com.catalent.shipment.domain.model.SObjectData
import com.catalent.shipment.domain.repository.SObjectRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesforceSObjectRepository @Inject constructor(
    private val clientProvider: ClientProvider
) : SObjectRepository, Loggable {

    private val account get() =
        SalesforceSDKManager.getInstance().userAccountManager.currentUser

    private val smartStore get() =
        MobileSyncSDKManager.getInstance().getSmartStore(account)

    override suspend fun registerSoup(
        sObjectType: String,
        displayField: String,
    ) = withContext(Dispatchers.IO) {
        val soupName = "${sObjectType}Soup"
        if (!smartStore.hasSoup(soupName)) {
            val indexSpecs = arrayOf(
                IndexSpec("Id", SmartStore.Type.string),
                IndexSpec(displayField, SmartStore.Type.string),
            )
            smartStore.registerSoup(soupName, indexSpecs)
            d("registerSoup", "registered $soupName")
        }
    }

    override suspend fun loadFromSmartStore(
        sObjectType: String,
        displayField: String,
        searchQuery: String,
        page: Int,
        pageSize: Int,
    ): List<SObjectData> = withContext(Dispatchers.IO) {
        val soupName = "${sObjectType}Soup"
        val querySpec = if (searchQuery.isEmpty()) {
            QuerySpec.buildAllQuerySpec(soupName, "Id", QuerySpec.Order.ascending, pageSize)
        } else {
            QuerySpec.buildLikeQuerySpec(
                soupName, displayField, "%$searchQuery%",
                "Id", QuerySpec.Order.ascending, pageSize,
            )
        }
        try {
            val results = smartStore.query(querySpec, page)
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

    override suspend fun fetchRemote(
        sObjectType: String,
        displayField: String,
        searchQuery: String,
        limit: Int,
        offset: Int,
    ): List<SObjectData> = withContext(Dispatchers.IO) {
        val restClient = clientProvider.client ?: return@withContext emptyList()
        val query = if (searchQuery.isNotEmpty()) {
            "SELECT Id, $displayField FROM $sObjectType WHERE $displayField LIKE '%$searchQuery%' ORDER BY Id LIMIT $limit"
        } else {
            "SELECT Id, $displayField FROM $sObjectType ORDER BY Id LIMIT $limit OFFSET $offset"
        }
        try {
            val request = RestRequest.getRequestForQuery("v60.0", query)
            val response = restClient.sendSync(request)
            if (response.isSuccess) {
                val records = response.asJSONObject().getJSONArray("records")
                d("fetchRemote", "fetched ${records.length()} records for $sObjectType")
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
                e("fetchRemote", "failed — ${response.asString()}")
                emptyList()
            }
        } catch (e: Exception) {
            e("fetchRemote", "exception", e)
            emptyList()
        }
    }

    override suspend fun upsertToSmartStore(
        sObjectType: String,
        items: List<SObjectData>,
        displayField: String,
    ) = withContext(Dispatchers.IO) {
        val soupName = "${sObjectType}Soup"
        if (!smartStore.hasSoup(soupName)) return@withContext
        items.forEach { item ->
            val json = JSONObject().apply {
                put("Id", item.id)
                put(displayField, item.name)
            }
            smartStore.upsert(soupName, json, "Id")
        }
    }
}