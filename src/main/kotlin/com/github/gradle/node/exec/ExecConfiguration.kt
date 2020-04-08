package com.github.gradle.node.exec

import org.gradle.process.ExecSpec
import java.io.File

internal data class ExecConfiguration(
        val executable: String,
        val args: List<String> = listOf(),
        var additionalBinPath: String? = null,
        val environment: Map<String, String> = mapOf(),
        val workingDir: File? = null,
        val ignoreExitValue: Boolean = false,
        val execOverrides: (ExecSpec.() -> Unit)? = null
)
