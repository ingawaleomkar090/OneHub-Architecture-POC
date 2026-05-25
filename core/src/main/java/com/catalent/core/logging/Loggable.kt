package com.catalent.core.logging

interface Loggable {
    val tag: String
        get() = this::class.java.simpleName

    fun d(method: String, message: String = "", throwable: Throwable? = null, destination: LogDestination = LogDestination.All) =
        AppLogger.d(tag, method, message, throwable, destination)

    fun i(method: String, message: String = "", throwable: Throwable? = null, destination: LogDestination = LogDestination.All) =
        AppLogger.i(tag, method, message, throwable, destination)

    fun w(method: String, message: String = "", throwable: Throwable? = null, destination: LogDestination = LogDestination.All) =
        AppLogger.w(tag, method, message, throwable, destination)

    fun e(method: String, message: String = "", throwable: Throwable? = null, destination: LogDestination = LogDestination.All) =
        AppLogger.e(tag, method, message, throwable, destination)
}