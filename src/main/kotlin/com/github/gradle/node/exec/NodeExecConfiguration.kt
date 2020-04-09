package com.github.gradle.node.exec

import org.gradle.process.ExecSpec
import java.io.File

internal data class NodeExecConfiguration(
        val command: List<String> = listOf(),
        val environment: Map<String, String> = mapOf(),
        val workingDir: File? = null,
        val ignoreExitValue: Boolean = false,
        val execOverrides: (ExecSpec.() -> Unit)? = null
)
