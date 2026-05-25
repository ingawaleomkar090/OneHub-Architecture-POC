package com.catalent.shipment.ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalent.core.logging.Loggable
import com.catalent.shipment.domain.model.SObjectListState
import com.catalent.shipment.domain.repository.SObjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SObjectListViewModel @Inject constructor(
    private val repository: SObjectRepository,
) : ViewModel(), Loggable {

    private val _contactsState = MutableStateFlow(SObjectListState())
    val contactsState: StateFlow<SObjectListState> = _contactsState.asStateFlow()

    private val _accountsState = MutableStateFlow(SObjectListState())
    val accountsState: StateFlow<SObjectListState> = _accountsState.asStateFlow()

    private val pageSize = 20

    fun initialLoad(isConnected: Boolean) {
        viewModelScope.launch {
            loadTab("Contact", "Name", isConnected, _contactsState)
        }
        viewModelScope.launch {
            loadTab("Account", "Name", isConnected, _accountsState)
        }
    }

    fun refresh(sObjectType: String, isConnected: Boolean) {
        val state = stateFor(sObjectType)
        viewModelScope.launch {
            state.value = state.value.copy(isRefreshing = true)
            if (isConnected) {
                fetchAndUpdate(sObjectType, "Name", state)
            } else {
                val local = repository.loadFromSmartStore(sObjectType, "Name")
                state.value = state.value.copy(items = local, isRefreshing = false)
            }
        }
    }

    fun loadMore(sObjectType: String, isConnected: Boolean) {
        val state = stateFor(sObjectType)
        if (state.value.isLoadingMore || !state.value.hasMoreRemote) return
        viewModelScope.launch {
            state.value = state.value.copy(isLoadingMore = true)
            val nextPage = state.value.items.size / pageSize
            val local = repository.loadFromSmartStore(
                sObjectType, "Name",
                page = nextPage,
                pageSize = pageSize,
            )
            if (local.isNotEmpty()) {
                val existing = state.value.items.map { it.id }.toSet()
                state.value = state.value.copy(
                    items = state.value.items + local.filter { it.id !in existing },
                    isLoadingMore = false,
                )
            } else if (isConnected) {
                val remote = repository.fetchRemote(
                    sObjectType, "Name",
                    offset = state.value.items.size,
                    limit = pageSize,
                )
                val existing = state.value.items.map { it.id }.toSet()
                state.value = state.value.copy(
                    items = state.value.items + remote.filter { it.id !in existing },
                    isLoadingMore = false,
                    hasMoreRemote = remote.size == pageSize,
                )
            } else {
                state.value = state.value.copy(isLoadingMore = false)
            }
        }
    }

    fun onSearchQueryChanged(sObjectType: String, query: String, isConnected: Boolean) {
        val state = stateFor(sObjectType)
        state.value = state.value.copy(searchQuery = query)
        viewModelScope.launch {
            val local = repository.loadFromSmartStore(
                sObjectType, "Name", searchQuery = query
            )
            state.value = state.value.copy(items = local)
            if (isConnected && query.isNotEmpty()) {
                val remote = repository.fetchRemote(
                    sObjectType, "Name", searchQuery = query
                )
                state.value = state.value.copy(items = remote)
            }
        }
    }

    private suspend fun loadTab(
        sObjectType: String,
        displayField: String,
        isConnected: Boolean,
        state: MutableStateFlow<SObjectListState>,
    ) {
        d("loadTab", "loading $sObjectType isConnected=$isConnected")
        repository.registerSoup(sObjectType, displayField)
        val local = repository.loadFromSmartStore(sObjectType, displayField)
        state.value = state.value.copy(
            items = local,
            isInitialLoading = false,
            isOffline = !isConnected,
        )
        if (isConnected) fetchAndUpdate(sObjectType, displayField, state)
    }

    private suspend fun fetchAndUpdate(
        sObjectType: String,
        displayField: String,
        state: MutableStateFlow<SObjectListState>,
    ) {
        state.value = state.value.copy(isRefreshing = true)
        val remote = repository.fetchRemote(
            sObjectType, displayField,
            searchQuery = state.value.searchQuery,
            limit = pageSize,
        )
        repository.upsertToSmartStore(sObjectType, remote, displayField)
        state.value = state.value.copy(
            items = remote,
            isRefreshing = false,
            hasMoreRemote = remote.size == pageSize,
            isOffline = false,
        )
        d("fetchAndUpdate", "$sObjectType fetched ${remote.size} records")
    }

    private fun stateFor(sObjectType: String): MutableStateFlow<SObjectListState> =
        when (sObjectType) {
            "Contact" -> _contactsState
            "Account" -> _accountsState
            else -> throw IllegalArgumentException("Unknown sObjectType: $sObjectType")
        }
}