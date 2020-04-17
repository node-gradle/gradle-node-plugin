package com.github.gradle.node.yarn.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmSetupTask
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import java.io.File
import java.util.*

/**
 * Setup a specific version of Yarn to be used by the build.
 */
open class YarnSetupTask : NpmSetupTask() {
    init {
        group = NodePlugin.NODE_GROUP
        description = "Setup a specific version of Yarn to be used by the build."
    }

    @Input
    override fun getInput(): Set<Any?> {
        val set: MutableSet<Any?> = HashSet()
        set.add(nodeExtension.download)
        set.add(nodeExtension.yarnVersion)
        return set
    }

    @OutputDirectory
    fun getYarnDir(): File {
        val variantComputer = VariantComputer()
        return variantComputer.computeYarnDir(nodeExtension)
    }

    override fun computeCommand(): List<String> {
        val version = nodeExtension.yarnVersion
        val yarnDir = getYarnDir()
        val yarnPackage = if (version.isNotBlank()) "yarn@$version" else "yarn"
        return listOf("install", "--global", "--no-save", *PROXY_SETTINGS.toTypedArray(),
                "--prefix", yarnDir.absolutePath, yarnPackage) + args
    }

    override fun isTaskEnabled(): Boolean {
        return true
    }

    companion object {
        const val NAME = "yarnSetup"
    }
}
