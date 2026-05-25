package com.catalent.shipment.domain.repository

import com.catalent.shipment.domain.model.SObjectData

interface SObjectRepository {
    suspend fun registerSoup(sObjectType: String, displayField: String)
    suspend fun loadFromSmartStore(
        sObjectType: String,
        displayField: String,
        searchQuery: String = "",
        page: Int = 0,
        pageSize: Int = 20,
    ): List<SObjectData>
    suspend fun fetchRemote(
        sObjectType: String,
        displayField: String,
        searchQuery: String = "",
        limit: Int = 20,
        offset: Int = 0,
    ): List<SObjectData>
    suspend fun upsertToSmartStore(
        sObjectType: String,
        items: List<SObjectData>,
        displayField: String,
    )
}