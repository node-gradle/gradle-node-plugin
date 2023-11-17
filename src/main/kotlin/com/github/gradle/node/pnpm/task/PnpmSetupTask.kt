package com.github.gradle.node.pnpm.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmSetupTask
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory

/**
 * pnpm install that only gets executed if gradle decides so.
 */
abstract class PnpmSetupTask : NpmSetupTask() {

    init {
        group = NodePlugin.PNPM_GROUP
        description = "Setup a specific version of pnpm to be used by the build."
    }

    @get:OutputDirectory
    val pnpmDir by lazy {
        val variantComputer = VariantComputer()
        variantComputer.computePnpmDir(nodeExtension)
    }

    override fun computeCommand(): List<String> {
        val version = nodeExtension.pnpmVersion.get()
        val pnpmDir = pnpmDir.get()
        val pnpmPackage = if (version.isNotBlank()) "pnpm@$version" else "pnpm"
        return listOf(
            "install",
            "--global",
            "--no-save",
            "--prefix",
            pnpmDir.asFile.absolutePath,
            pnpmPackage
        ) + args.get()
    }

    override fun isTaskEnabled(): Boolean {
        return true
    }

    companion object {
        const val NAME = "pnpmSetup"
    }

}
