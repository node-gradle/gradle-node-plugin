package com.moowork.gradle.node.npm

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import groovy.lang.Closure
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.invoke
import java.io.File

/**
 * npm install that only gets executed if gradle decides so.
 */
open class NpmInstallTask : NpmTask() {

    @Suppress("MemberVisibilityCanBePrivate") // Configurable
    var nodeModulesOutputFilter: (ConfigurableFileTree.() -> Unit)? = null

    private val extension = NodeExtension[project]

    init {
        group = NodePlugin.NODE_GROUP
        description = "Install node packages from package.json."
        dependsOn(NpmSetupTask.NAME)
        project.afterEvaluate {
            npmCommand = listOf(extension.npmInstallCommand)
        }
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFile
    protected fun getPackageJsonFile(): File? {
        val file = File(extension.nodeModulesDir, "package.json")
        return file.takeIf { it.exists() }
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    @InputFile
    protected fun getNpmShrinkwrap(): File? {
        val file = File(extension.nodeModulesDir, "npm-shrinkwrap.json")
        return file.takeIf { it.exists() }
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    @InputFile
    protected fun getPackageLockFileAsInput(): File? {
        val lockFile = File(extension.nodeModulesDir, "package-lock.json")
        return lockFile.takeIf { npmCommand[0] == "ci" && it.exists() }
    }

    @Optional
    @OutputFile
    protected fun getPackageLockFileAsOutput(): File? {
        val file = File(extension.nodeModulesDir, "package-lock.json")
        return file.takeIf { npmCommand[0] == "install" && it.exists() }
    }

    @OutputFiles
    protected fun getNodeModulesDir(): ConfigurableFileTree {
        val nodeModulesDirectory = File(extension.nodeModulesDir, "node_modules")
        val nodeModulesFileTree = project.fileTree(nodeModulesDirectory)
        nodeModulesOutputFilter?.invoke(nodeModulesFileTree)
        return nodeModulesFileTree
    }

    // Configurable; Groovy support
    @Suppress("unused")
    fun setNodeModulesOutputFilter(nodeModulesOutputFilter: Closure<ConfigurableFileTree>) {
        this.nodeModulesOutputFilter = { nodeModulesOutputFilter.invoke(this) }
    }

    companion object {
        const val NAME = "npmInstall"
    }
}
