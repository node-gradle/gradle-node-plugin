package com.github.gradle.node.npm.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import groovy.lang.Closure
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.invoke
import java.io.File

/**
 * npm install that only gets executed if gradle decides so.
 */
open class NpmInstallTask : NpmTask() {
    private val nodeExtension by lazy { NodeExtension[project] }
    @get:Internal
    var nodeModulesOutputFilter: (ConfigurableFileTree.() -> Unit)? = null

    init {
        group = NodePlugin.NODE_GROUP
        description = "Install node packages from package.json."
        dependsOn(NpmSetupTask.NAME)
        project.afterEvaluate {
            npmCommand = listOf(nodeExtension.npmInstallCommand)

            val nodeModulesDirectory = File(nodeExtension.nodeModulesDir, "node_modules")
            if (nodeModulesOutputFilter != null) {
                val nodeModulesFileTree = project.fileTree(nodeModulesDirectory)
                nodeModulesOutputFilter?.invoke(nodeModulesFileTree)
                outputs.files(nodeModulesFileTree)
            } else {
                outputs.dir(nodeModulesDirectory)
            }
        }
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFile
    protected fun getPackageJsonFile(): File? {
        val file = File(nodeExtension.nodeModulesDir, "package.json")
        return file.takeIf { it.exists() }
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    @InputFile
    protected fun getNpmShrinkwrap(): File? {
        val file = File(nodeExtension.nodeModulesDir, "npm-shrinkwrap.json")
        return file.takeIf { it.exists() }
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    @InputFile
    protected fun getPackageLockFileAsInput(): File? {
        val lockFile = File(nodeExtension.nodeModulesDir, "package-lock.json")
        return lockFile.takeIf { npmCommand[0] == "ci" && it.exists() }
    }

    @Optional
    @OutputFile
    protected fun getPackageLockFileAsOutput(): File? {
        val file = File(nodeExtension.nodeModulesDir, "package-lock.json")
        return file.takeIf { npmCommand[0] == "install" && it.exists() }
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
