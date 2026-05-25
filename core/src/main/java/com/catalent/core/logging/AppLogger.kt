package com.catalent.core.logging

import android.content.Context

object AppLogger : OneHubLogger {

    private val loggers = mutableListOf<OneHubLogger>()
    private lateinit var consoleLogger: ConsoleLogger

    fun init(context: Context, enableLogging: Boolean = true) {
        consoleLogger = ConsoleLogger()
        val list = buildList {
            if (enableLogging) add(consoleLogger)
        }
        loggers.addAll(list)
    }

    override fun d(tag: String, method: String, message: String, throwable: Throwable?, destination: LogDestination) =
        route(destination) { it.d(tag, method, message, throwable, destination) }

    override fun i(tag: String, method: String, message: String, throwable: Throwable?, destination: LogDestination) =
        route(destination) { it.i(tag, method, message, throwable, destination) }

    override fun w(tag: String, method: String, message: String, throwable: Throwable?, destination: LogDestination) =
        route(destination) { it.w(tag, method, message, throwable, destination) }

    override fun e(tag: String, method: String, message: String, throwable: Throwable?, destination: LogDestination) =
        route(destination) { it.e(tag, method, message, throwable, destination) }

    private fun route(destination: LogDestination, action: (OneHubLogger) -> Unit) {
        when (destination) {
            LogDestination.ConsoleOnly -> action(consoleLogger)
            LogDestination.All -> loggers.forEach(action)
        }
    }
}