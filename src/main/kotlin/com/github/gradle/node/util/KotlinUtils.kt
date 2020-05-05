package com.github.gradle.node.util

import java.util.concurrent.TimeUnit

/**
 * Executes the given command and returns its output.
 *
 * This is based on an aggregate of the answers provided here: [https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code]
 */
internal fun execute(vararg args: String, timeout: Long = 60): String {
    return ProcessBuilder(args.toList())
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()
            .apply { waitFor(timeout, TimeUnit.SECONDS) }
            .inputStream.bufferedReader().readText().trim()
}

/**
 * Transforms the receiver with the given [transform] if the provided [predicate] evaluates to `true`. Otherwise, the receiver is returned.
 */
internal inline fun <T : Any?> T.mapIf(predicate: (T) -> Boolean, transform: (T) -> T): T = if (predicate(this)) transform(this) else this
