package com.github.gradle.node.npm.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

internal typealias CommandExecComputer = (variantComputer: VariantComputer, nodeExtension: NodeExtension,
                                          npmBinDir: Provider<Directory>) -> Provider<String>

internal data class NpmExecConfiguration(
        val command: String,
        val commandExecComputer: CommandExecComputer
)
