package com.github.gradle.node.npm.exec

internal data class NpmExecConfiguration(
        val command: String,
        val localCommandScript: String,
        val commandScript: String
)
