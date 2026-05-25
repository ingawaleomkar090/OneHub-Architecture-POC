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
    val contactsState: StateFlow<SObjectListState> = contactSearchQuery
        .flatMapLatest { query ->
            repository.getSObjects("Contact", "Name", query)
        }
        .combine(_isRefreshing) { items, refreshing ->
            SObjectListState(
                items = items,
                isRefreshing = refreshing,
                isInitialLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SObjectListState(isInitialLoading = true)
        )

    // Reactive Flow for Accounts
    val accountsState: StateFlow<SObjectListState> = accountSearchQuery
        .flatMapLatest { query ->
            repository.getSObjects("Account", "Name", query)
        }
        .combine(_isRefreshing) { items, refreshing ->
            SObjectListState(
                items = items,
                isRefreshing = refreshing,
                isInitialLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SObjectListState(isInitialLoading = true)
        )

    fun initialLoad(isConnected: Boolean) {
        if (isConnected) {
            refreshAll()
        }
    }

    fun refresh(sObjectType: String, isConnected: Boolean) {
        if (isConnected) {
            viewModelScope.launch {
                _isRefreshing.value = true
                try {
                    repository.sync(sObjectType, "Name")
                } catch (e: Exception) {
                    e("refresh", "failed to sync $sObjectType", e)
                } finally {
                    _isRefreshing.value = false
                }
            }
        }
    }

    private fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.sync("Contact", "Name")
                repository.sync("Account", "Name")
            } catch (e: Exception) {
                e("refreshAll", "failed to sync", e)
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
        // If connected and query is not empty, we might want to trigger a sync for the search
        // But for now, we rely on the reactive flow to show local results immediately.
    }

    fun loadMore(sObjectType: String, isConnected: Boolean) {
        // Pagination logic can be added here if needed, 
        // for now we've simplified to show top 100 in the reactive flow
    }
}