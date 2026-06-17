package com.catalent.shipment.domain.repository

import com.catalent.shipment.domain.model.SObjectData
import kotlinx.coroutines.flow.Flow

interface SObjectRepository {
    /**
     * Returns a stream of data for the given [sObjectType].
     * Emits the local data from SmartStore immediately, and then 
     * emits again whenever the local data is updated.
     */
    fun getSObjects(
        sObjectType: String,
        displayField: String,
        searchQuery: String = ""
    ): Flow<List<SObjectData>>

    /**
     * Triggers a remote fetch from Salesforce and updates the local SmartStore.
     * The [getSObjects] flow will automatically emit the new data.
     */
    suspend fun sync(
        sObjectType: String,
        displayField: String
    )

    /**
     * Searches for SObjects remotely if not found locally or to refresh search results.
     */
    suspend fun searchRemotely(
        sObjectType: String,
        displayField: String,
        query: String
    )

    /**
     * Loads the next page of data.
     */
    suspend fun loadMore(
        sObjectType: String,
        displayField: String,
        offset: Int
    )
}