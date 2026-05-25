package com.catalent.core.network

/**
 * A marker interface representing a platform-specific network client.
 * This allows the core module to remain independent of specific SDKs like Salesforce.
 */
interface AppClient

/**
 * A generic wrapper for the raw platform client.
 * @property rawClient The underlying SDK client (e.g., Salesforce RestClient).
 */
data class RawClientWrapper(val rawClient: Any) : AppClient