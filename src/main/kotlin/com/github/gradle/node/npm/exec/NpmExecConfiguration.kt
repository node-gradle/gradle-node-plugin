package com.github.gradle.node.npm.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.variant.VariantComputer
import java.io.File

internal typealias CommandExecComputer = (variantComputer: VariantComputer, nodeExtension: NodeExtension,
                                          npmBinDir: File) -> String

internal data class NpmExecConfiguration(
        val command: String,
        val commandExecComputer: CommandExecComputer
)
