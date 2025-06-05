package com.github.gradle.node.npm.task

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
 * npm install that only gets executed if gradle decides so.
 */
abstract class NpmInstallTask : NpmTask() {
    @get:Internal
    val nodeModulesOutputFilter =
        objects.property<Action<ConfigurableFileTree>>()

    @get:Internal
    val fastInstall = objects.property<Boolean>()

    init {
        group = NodePlugin.NPM_GROUP
        description = "Install node packages from package.json."
        dependsOn(NpmSetupTask.NAME)
        npmCommand.set(nodeExtension.npmInstallCommand.map { listOf(it) })
        fastInstall.set(nodeExtension.fastNpmInstall)
        outputs.doNotCacheIf("With fastNpmInstall there's no output to cache") {
            fastInstall.get()
        }
    }

    @PathSensitive(RELATIVE)
    @InputFile
    @SkipWhenEmpty
    protected fun getPackageJsonFile(): File? {
        return projectFileIfExists("package.json").orNull
    }

    @PathSensitive(RELATIVE)
    @Optional
    @InputFile
    protected fun getNpmShrinkwrap(): File? {
        return projectFileIfExists("npm-shrinkwrap.json").orNull
    }

    @PathSensitive(RELATIVE)
    @Optional
    @InputFile
    protected fun getPackageLockFileAsInput(): File? {
        return npmCommand.flatMap { command ->
            if (command[0] == "ci") projectFileIfExists("package-lock.json") else providers.provider { null }
        }.orNull
    }

    @PathSensitive(RELATIVE)
    @Optional
    @InputFile
    protected fun getYarnLockFile(): File? {
        return projectFileIfExists("yarn.lock").orNull
    }

    @Optional
    @OutputFile
    protected fun getPackageLockFileAsOutput(): File? {
        return npmCommand.flatMap { command ->
            if (command[0] == "install") projectFileIfExists("package-lock.json") else providers.provider { null }
        }.orNull
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
        return if (filter == null && !fastInstall.get()) nodeExtension.nodeProjectDir.dir("node_modules")
        else providers.provider { null }
    }

    @Optional
    @OutputFiles
    @Suppress("unused")
    protected fun getNodeModulesFiles(): Provider<FileTree> {
        return if (fastInstall.get()) {
            providers.provider { null }
        } else {
            val nodeModulesDirectoryProvider = nodeExtension.nodeProjectDir.dir("node_modules")
            zip(nodeModulesDirectoryProvider, nodeModulesOutputFilter)
                .flatMap { (nodeModulesDirectory, nodeModulesOutputFilter) ->
                    if (nodeModulesOutputFilter != null) {
                        val fileTree = projectHelper.fileTree(nodeModulesDirectory)
                        nodeModulesOutputFilter.execute(fileTree)
                        providers.provider { fileTree }
                    } else providers.provider { null }
                }
        }
    }

    @Optional
    @OutputFile
    protected fun getNodeModulesPackageLock(): File? {
        if (isLegacyNpm()) {
            return null
        }

        return projectFileIfExists("node_modules/.package-lock.json").orNull
    }

    /**
     * Is our npm likely to be lower than 7?
     */
    private fun isLegacyNpm(): Boolean {
        if (nodeExtension.oldNpm.get()) {
            return true
        }

        val npmVersion = nodeExtension.npmVersion.get()
        if (npmVersion.isBlank()) {
            if (nodeExtension.version.get().split('.').first().toInt() <= 14)
                return true
        } else if (npmVersion.split('.').first().toInt() < 7) {
            return true
        }

        return false
    }

    // For DSL
    @Suppress("unused")
    fun nodeModulesOutputFilter(nodeModulesOutputFilter: Action<ConfigurableFileTree>) {
        this.nodeModulesOutputFilter.set(nodeModulesOutputFilter)
    }

    companion object {
        const val NAME = "npmInstall"
    }
}
