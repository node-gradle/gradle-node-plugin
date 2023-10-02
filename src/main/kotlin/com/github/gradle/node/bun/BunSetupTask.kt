package com.github.gradle.node.bun

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmSetupTask
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory

/**
 * bun install that only gets executed if gradle decides so.
 */
abstract class BunSetupTask : NpmSetupTask() {

    init {
        group = NodePlugin.BUN_GROUP
        description = "Setup a specific version of Bun to be used by the build."
    }

    @Input
    override fun getVersion(): Provider<String> {
        return nodeExtension.bunVersion
    }

    @get:OutputDirectory
    val bunDir by lazy {
        val variantComputer = VariantComputer()
        variantComputer.computePnpmDir(nodeExtension)
    }

    override fun computeCommand(): List<String> {
        val version = nodeExtension.bunVersion.get()
        val bunDir = bunDir.get()
        val bunPackage = if (version.isNotBlank()) "bun@$version" else "bun"
        return listOf(
            "install",
            "--global",
            "--no-save",
            "--prefix",
            bunDir.asFile.absolutePath,
            bunPackage
        ) + args.get()
    }

    override fun isTaskEnabled(): Boolean {
        return true
    }

    companion object {
        const val NAME = "bunSetup"
    }

}
