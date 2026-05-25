package com.catalent.core.network

import com.salesforce.androidsdk.rest.RestClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientProvider @Inject constructor() {

    private val listeners = mutableListOf<ClientListener>()

    var client: RestClient? = null
        private set

    fun addListener(listener: ClientListener) {
        listeners.add(listener)
    }

    fun onClientAvailable(client: RestClient) {
        this.client = client
        listeners.forEach { it.onClientAvailable(client) }
    }

    fun onClientRemoved() {
        client = null
        listeners.forEach { it.onClientRemoved() }
    }

    interface ClientListener {
        fun onClientAvailable(client: RestClient)
        fun onClientRemoved()
    }
}