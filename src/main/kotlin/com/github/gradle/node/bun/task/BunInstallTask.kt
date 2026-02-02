package com.github.gradle.node.bun.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.util.zip
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.kotlin.dsl.property
import java.io.File

/**
 * bun install that only gets executed if gradle decides so.
 */
abstract class BunInstallTask : BunTask() {

    @get:Internal
    val nodeModulesOutputFilter =
            objects.property<Action<ConfigurableFileTree>>()


    init {
        group = NodePlugin.BUN_GROUP
        description = "Install packages from package.json."
        dependsOn(BunSetupTask.NAME)
        bunCommand.set(nodeExtension.npmInstallCommand.map {
            when(it) {
                "ci" -> listOf("install", "--save-text-lockfile")
                else -> listOf(it)
            }
        })
    }

    @PathSensitive(RELATIVE)
    @InputFile
    protected fun getPackageJsonFile(): File? {
        return projectFileIfExists("package.json").orNull
    }

    @Optional
    @OutputFile
    protected fun getBunLockAsOutput(): File? {
        return projectFileIfExists("bun.lock").orNull
    }

    private fun projectFileIfExists(name: String): Provider<File> {
        return nodeExtension.nodeProjectDir.map { it.file(name).asFile }
            .flatMap { if (it.exists()) providers.provider { it } else providers.provider { null } }
    }

    @Optional
    @OutputDirectory
    @Suppress("unused")
    protected fun getNodeModulesDirectory(): Provider<Directory> {
        val filter = nodeModulesOutputFilter.orNull
        return if (filter == null) nodeExtension.nodeProjectDir.dir("node_modules")
        else providers.provider { null }
    }

    @Optional
    @OutputFiles
    @Suppress("unused")
    protected fun getNodeModulesFiles(): Provider<FileTree> {
        val nodeModulesDirectoryProvider = nodeExtension.nodeProjectDir.dir("node_modules")
        return zip(nodeModulesDirectoryProvider, nodeModulesOutputFilter)
                .flatMap { (nodeModulesDirectory, nodeModulesOutputFilter) ->
                    if (nodeModulesOutputFilter != null) {
                        val fileTree = objects.fileTree().from(nodeModulesDirectory)
                        nodeModulesOutputFilter.execute(fileTree)
                        providers.provider { fileTree }
                    } else providers.provider { null }
                }
    }

    // For DSL
    @Suppress("unused")
    fun nodeModulesOutputFilter(nodeModulesOutputFilter: Action<ConfigurableFileTree>) {
        this.nodeModulesOutputFilter.set(nodeModulesOutputFilter)
    }

    companion object {
        const val NAME = "bunInstall"
    }

}
