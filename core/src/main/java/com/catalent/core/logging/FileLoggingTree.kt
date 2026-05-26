package com.catalent.core.logging

import android.util.Log
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors

/**
 * A custom [Timber.Tree] that writes logs to local files with timestamp-based naming.
 *
 * Requirements:
 * 1. File Rolling: Max 5MB per file, max 10 files, FIFO rotation via timestamps.
 * 2. Retention: Deletes files older than 7 days.
 * 3. Performance: Thread-safe, non-blocking via single-thread executor.
 * 4. Formatting: yyyy-MM-dd HH:mm:ss.SSS LEVEL/TAG: MESSAGE
 * 5. File Name: log_yyyyMMdd_HHmmss.log
 */
class FileLoggingTree(private val logDir: File) : Timber.Tree() {

    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "file-logger").apply { isDaemon = true }
    }
    private val entryFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private val fileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    @Volatile private var currentFile: File? = null
    private var bufferedWriter: BufferedWriter? = null

    companion object {
        private const val MAX_FILE_SIZE   = 5 * 1024 * 1024L  // 5 MB
        private const val MAX_FILE_COUNT  = 10
        private const val RETENTION_DAYS  = 7L
        private const val PREFIX          = "log_"
        private const val EXTENSION       = ".log"
        private const val CLEANUP_INTERVAL_MS = 6 * 60 * 60 * 1000L // every 6 hrs
    }

    private var lastCleanupTime = 0L

    init {
        logDir.mkdirs()
        executor.execute { cleanOldLogs() }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val timestamp = LocalDateTime.now().format(entryFormatter)
        val entry = buildEntry(timestamp, priority, tag, message, t)
        executor.execute { writeEntry(entry) }
    }

    private fun buildEntry(
        timestamp: String,
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ): String = buildString {
        append(timestamp)
        append(" ")
        append(priorityLabel(priority))
        append("/")
        append(tag ?: "App")
        append(": ")
        append(message)
        if (t != null) append("\n").append(Log.getStackTraceString(t))
        append("\n")
    }

    // ------------------------------------------------------------------ file I/O (executor thread only)
    private fun writeEntry(entry: String) {
        try {
            maybeRunPeriodicCleanup()
            val writer = getOrRotateWriter()
            writer.write(entry)
            writer.flush()
        } catch (e: IOException) {
            closeWriter()
            Log.e("FileLoggingTree", "Write failed", e)
        }
    }

    /**
     * Returns the current BufferedWriter, rotating to a new file if the
     * active file has reached MAX_FILE_SIZE.
     */
    private fun getOrRotateWriter(): BufferedWriter {
        if (currentFile == null) {
            val latest = logFiles().sortedBy { it.name }.lastOrNull()
            if (latest != null && latest.length() < MAX_FILE_SIZE) {
                currentFile = latest
            }
        }
        val active = currentFile
        if (active != null && active.length() < MAX_FILE_SIZE) {
            return bufferedWriter ?: openWriter(active)
        }
        return rotate()
    }

    private fun rotate(): BufferedWriter {
        closeWriter()
        cleanOldLogs()
        pruneOldestIfNeeded()
        val newFile = File(logDir, "$PREFIX${LocalDateTime.now().format(fileNameFormatter)}$EXTENSION")
        currentFile = newFile
        return openWriter(newFile).also { writeFileHeader(it) }
    }

    private fun openWriter(file: File): BufferedWriter {
        return BufferedWriter(FileWriter(file, true), 8 * 1024).also {
            bufferedWriter = it
        }
    }

    private fun closeWriter() {
        try { bufferedWriter?.flush(); bufferedWriter?.close() } catch (_: IOException) {}
        bufferedWriter = null
    }

    private fun writeFileHeader(writer: BufferedWriter) {
        val header = buildString {
            append("===========================\n")
            append("Log session started: ${LocalDateTime.now()}\n")
            append("===========================\n")
        }
        writer.write(header)
    }

    // ------------------------------------------------------------------ file management

    private fun pruneOldestIfNeeded() {
        val files = logFiles().sortedBy { it.name }  // ascending = oldest first
        if (files.size >= MAX_FILE_COUNT) {
            files.first().delete()
        }
    }

    private fun maybeRunPeriodicCleanup() {
        val now = System.currentTimeMillis()
        if (now - lastCleanupTime >= CLEANUP_INTERVAL_MS) {
            lastCleanupTime = now
            cleanOldLogs()
        }
    }

    private fun cleanOldLogs() {
        val cutoff = Instant.now().minus(RETENTION_DAYS, ChronoUnit.DAYS)
        logFiles().filter {
            Instant.ofEpochMilli(it.lastModified()).isBefore(cutoff)
        }.forEach { it.delete() }
    }

    private fun logFiles(): List<File> =
        logDir.listFiles { f -> f.name.startsWith(PREFIX) && f.name.endsWith(EXTENSION) }
            ?.toList() ?: emptyList()

    // ------------------------------------------------------------------ helpers

    private fun priorityLabel(priority: Int) = when (priority) {
        Log.VERBOSE -> "V"
        Log.DEBUG   -> "D"
        Log.INFO    -> "I"
        Log.WARN    -> "W"
        Log.ERROR   -> "E"
        Log.ASSERT  -> "A"
        else        -> "?"
    }
}
