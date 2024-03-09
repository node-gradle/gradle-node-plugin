package com.github.gradle.node.pnpm.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.util.zip
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

/**
 * pnpm install that only gets executed if gradle decides so.
 */
abstract class PnpmInstallTask : PnpmTask() {
    @get:Inject
    abstract val factory: ObjectFactory

    @get:Internal
    val nodeModulesOutputFilter =
            objects.property<Action<ConfigurableFileTree>>()


    init {
        group = NodePlugin.PNPM_GROUP
        description = "Install node packages from package.json."
        dependsOn(PnpmSetupTask.NAME)
        pnpmCommand.set(nodeExtension.npmInstallCommand.map { listOf(it) })
    }

    @PathSensitive(RELATIVE)
    @InputFile
    protected fun getPackageJsonFile(): File? {
        return projectFileIfExists("package.json").orNull
    }

    @Optional
    @OutputFile
    protected fun getPnpmLockAsOutput(): File? {
        return projectFileIfExists("pnpm-lock.yaml").orNull
    }

    private fun projectFileIfExists(name: String): Provider<File?> {
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
                        val fileTree = factory.fileTree().from(nodeModulesDirectory)
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
        const val NAME = "pnpmInstall"
    }

}
