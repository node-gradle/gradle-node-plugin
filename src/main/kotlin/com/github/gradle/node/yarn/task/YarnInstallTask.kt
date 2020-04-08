package com.github.gradle.node.yarn.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import groovy.lang.Closure
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.invoke
import java.io.File

/**
 * yarn install that only gets executed if gradle decides so.
 */
open class YarnInstallTask : YarnTask() {
    private val nodeExtension by lazy { NodeExtension[project] }
    @get:Internal
    var nodeModulesOutputFilter: (ConfigurableFileTree.() -> Unit)? = null

    init {
        group = NodePlugin.NODE_GROUP
        description = "Install node packages using Yarn."
        dependsOn(YarnSetupTask.NAME)

        project.afterEvaluate {
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
    @Optional
    @InputFile
    protected fun getPackageJsonFile(): File? {
        val packageJsonFile = File(nodeExtension.nodeModulesDir, "package.json")
        return packageJsonFile.takeIf { it.exists() }
    }

    @PathSensitive(PathSensitivity.RELATIVE)
    @Optional
    @InputFile
    protected fun getYarnLockFile(): File? {
        val lockFile = File(nodeExtension.nodeModulesDir, "yarn.lock")
        return lockFile.takeIf { it.exists() }
    }

    // For Groovy DSL
    @Suppress("unused")
    fun setNodeModulesOutputFilter(nodeModulesOutputFilter: Closure<ConfigurableFileTree>) {
        this.nodeModulesOutputFilter = { nodeModulesOutputFilter.invoke(this) }
    }

    companion object {
        const val NAME = "yarn"
    }
}
