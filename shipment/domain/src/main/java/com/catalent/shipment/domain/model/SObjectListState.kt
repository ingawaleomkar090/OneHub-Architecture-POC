package com.catalent.shipment.domain.model

data class SObjectListState(
    val items: List<SObjectData> = emptyList(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreRemote: Boolean = true,
    val searchQuery: String = "",
    val isOffline: Boolean = false,
    val error: String? = null,
)
