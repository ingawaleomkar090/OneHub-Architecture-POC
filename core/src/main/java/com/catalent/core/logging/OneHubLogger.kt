package com.catalent.core.logging

interface OneHubLogger {
    fun d(tag: String, method: String, message: String = "", throwable: Throwable? = null, destination: LogDestination = LogDestination.All)
    fun i(tag: String, method: String, message: String = "", throwable: Throwable? = null, destination: LogDestination = LogDestination.All)
    fun w(tag: String, method: String, message: String = "", throwable: Throwable? = null, destination: LogDestination = LogDestination.All)
    fun e(tag: String, method: String, message: String = "", throwable: Throwable? = null, destination: LogDestination = LogDestination.All)
}