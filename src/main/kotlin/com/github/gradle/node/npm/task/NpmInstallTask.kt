package com.github.gradle.node.npm.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import java.io.File

/**
 * npm install that only gets executed if gradle decides so.
 */
open class NpmInstallTask : NpmTask() {
    private val nodeExtension by lazy { NodeExtension[project] }

    @get:Internal
    val nodeModulesOutputFilter =
            project.objects.property<Action<ConfigurableFileTree>>()

    init {
        group = NodePlugin.NODE_GROUP
        description = "Install node packages from package.json."
        dependsOn(NpmSetupTask.NAME)
        npmCommand.set(nodeExtension.npmInstallCommand.map { listOf(it) })
        project.afterEvaluate {
            val nodeModulesDirectory = nodeExtension.nodeModulesDir.get().dir("node_modules")
            val filter = nodeModulesOutputFilter.orNull
            if (filter != null) {
                val nodeModulesFileTree = project.fileTree(nodeModulesDirectory)
                filter.invoke(nodeModulesFileTree)
                outputs.files(nodeModulesFileTree)
            } else {
                outputs.dir(nodeModulesDirectory)
            }
        }
    }

    @PathSensitive(RELATIVE)
    @InputFile
    protected fun getPackageJsonFile(): Provider<File?> {
        return projectFileIfExists("package.json")
    }

    @PathSensitive(RELATIVE)
    @Optional
    @InputFile
    protected fun getNpmShrinkwrap(): Provider<File?> {
        return projectFileIfExists("npm-shrinkwrap.json")
    }

    @PathSensitive(RELATIVE)
    @Optional
    @InputFile
    protected fun getPackageLockFileAsInput(): Provider<File?> {
        return npmCommand.flatMap { command ->
            if (command[0] == "ci") projectFileIfExists("package-lock.json") else project.provider { null }
        }
    }

    @Optional
    @OutputFile
    protected fun getPackageLockFileAsOutput(): Provider<File?> {
        return npmCommand.flatMap { command ->
            if (command[0] == "install") projectFileIfExists("package-lock.json") else project.provider { null }
        }
    }

    // For DSL
    @Suppress("unused")
    fun nodeModulesOutputFilter(nodeModulesOutputFilter: Action<ConfigurableFileTree>) {
        this.nodeModulesOutputFilter.set(nodeModulesOutputFilter)
    }

    private fun projectFileIfExists(name: String): Provider<File?> {
        return nodeExtension.nodeModulesDir.map { it.file(name).asFile }
                .flatMap { if (it.exists()) project.providers.provider { it } else project.providers.provider { null } }
    }

    companion object {
        const val NAME = "npmInstall"
    }
}
