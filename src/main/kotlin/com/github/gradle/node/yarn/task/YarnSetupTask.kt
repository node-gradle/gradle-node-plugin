package com.github.gradle.node.yarn.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.task.NpmSetupTask
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import java.io.File

/**
 * Setup a specific version of Yarn to be used by the build.
 */
abstract class YarnSetupTask : NpmSetupTask() {
    init {
        group = NodePlugin.YARN_GROUP
        description = "Setup a specific version of Yarn to be used by the build."
    }
    
    @get:Input
    override val args = objects.listProperty<String>()

    @Input
    override fun getVersion(): Provider<String> {
        return nodeExtension.yarnVersion
    }

    @get:OutputDirectory
    val yarnDir by lazy {
        variantComputer.computeYarnDir(nodeExtension)
    }

    override fun computeCommand(): List<String> {
        val version = nodeExtension.yarnVersion.get()
        val yarnDir = yarnDir.get()
        val yarnPackage = if (version.isNotBlank()) "yarn@$version" else "yarn"
        // npm < 7 creates the directory if it's missing, >= 7 fails if it's missing
        // create the directory since we use npm to install yarn.
        File(yarnDir.asFile, "lib").mkdirs()
        return listOf("install", "--global", "--no-save", "--prefix", yarnDir.asFile.absolutePath, yarnPackage)
                .plus(args.get())
    }

    override fun isTaskEnabled(): Boolean {
        return true
    }

    companion object {
        const val NAME = "yarnSetup"
    }
}
