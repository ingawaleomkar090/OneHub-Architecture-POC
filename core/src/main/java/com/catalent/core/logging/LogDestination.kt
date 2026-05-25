package com.catalent.core.logging

enum class LogDestination {
    ConsoleOnly, // only prints to Logcat.
    All // sends to all registered destinations. Right now that's just ConsoleLogger but when you add Crashlytics or Datadog later it goes there too.
}