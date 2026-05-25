package com.catalent.core.logging

import timber.log.Timber

class ConsoleLogger : OneHubLogger {
    override fun d(tag: String, method: String, message: String, throwable: Throwable?, destination: LogDestination) =
        Timber.tag(tag).d(throwable, "[$method] $message")

    override fun i(tag: String, method: String, message: String, throwable: Throwable?, destination: LogDestination) =
        Timber.tag(tag).i(throwable, "[$method] $message")

    override fun w(tag: String, method: String, message: String, throwable: Throwable?, destination: LogDestination) =
        Timber.tag(tag).w(throwable, "[$method] $message")

    override fun e(tag: String, method: String, message: String, throwable: Throwable?, destination: LogDestination) =
        Timber.tag(tag).e(throwable, "[$method] $message")
}