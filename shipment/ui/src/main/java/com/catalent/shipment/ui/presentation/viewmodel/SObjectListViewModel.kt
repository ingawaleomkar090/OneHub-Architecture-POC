package com.catalent.shipment.ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catalent.core.logging.Loggable
import com.catalent.shipment.domain.model.SObjectListState
import com.catalent.shipment.domain.repository.SObjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SObjectListViewModel @Inject constructor(
    private val repository: SObjectRepository,
) : ViewModel(), Loggable {

    private val contactSearchQuery = MutableStateFlow("")
    private val accountSearchQuery = MutableStateFlow("")

    private val _isRefreshing = MutableStateFlow(false)

    // Reactive Flow for Contacts
    val contactsState: StateFlow<SObjectListState> = combine(
        contactSearchQuery,
        _isRefreshing
    ) { query, refreshing -> query to refreshing }
        .flatMapLatest { (query, refreshing) ->
            repository.getSObjects("Contact", "Name", query)
                .map { items ->
                    SObjectListState(
                        items = items,
                        isRefreshing = refreshing,
                        isInitialLoading = false,
                        searchQuery = query
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SObjectListState(isInitialLoading = true)
        )

    // Reactive Flow for Accounts
    val accountsState: StateFlow<SObjectListState> = combine(
        accountSearchQuery,
        _isRefreshing
    ) { query, refreshing -> query to refreshing }
        .flatMapLatest { (query, refreshing) ->
            repository.getSObjects("Account", "Name", query)
                .map { items ->
                    SObjectListState(
                        items = items,
                        isRefreshing = refreshing,
                        isInitialLoading = false,
                        searchQuery = query
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SObjectListState(isInitialLoading = true)
        )

    fun initialLoad(isConnected: Boolean) {
        // Initial load is now handled by the stateIn flow which emits from SmartStore
        // We only trigger sync if connected
        if (isConnected) {
            refreshAll()
        }
    }

    fun refresh(sObjectType: String, isConnected: Boolean) {
        if (!isConnected) return

        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.sync(sObjectType, "Name")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.sync("Contact", "Name")
                repository.sync("Account", "Name")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onSearchQueryChanged(sObjectType: String, query: String, isConnected: Boolean) {
        when (sObjectType) {
            "Contact" -> contactSearchQuery.value = query
            "Account" -> accountSearchQuery.value = query
        }
        
        if (isConnected && query.isNotEmpty()) {
            viewModelScope.launch {
                repository.searchRemotely(sObjectType, "Name", query)
            }
        }
    }

    fun loadMore(sObjectType: String, isConnected: Boolean) {
        if (!isConnected) return

        val currentState = if (sObjectType == "Contact") contactsState.value else accountsState.value
        if (currentState.isLoadingMore) return

        viewModelScope.launch {
            try {
                repository.loadMore(sObjectType, "Name", currentState.items.size)
            } catch (e: Exception) {
                e("loadMore", "Failed to load more for $sObjectType", e)
            }
        }
    }
}