package com.catalent.core.network

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class ClientProvider @Inject constructor() {

    private val _client = MutableStateFlow<AppClient?>(null)
    val client: StateFlow<AppClient?> = _client.asStateFlow()

    fun onClientAvailable(client: AppClient) {
        _client.value = client
    }

    fun onClientRemoved() {
        _client.value = null
    }
}