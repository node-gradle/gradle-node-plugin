package com.github.gradle.node

import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmSetupTask
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.task.NodeSetupTask
import com.github.gradle.node.task.NodeTask
import com.github.gradle.node.yarn.task.YarnInstallTask
import com.github.gradle.node.yarn.task.YarnSetupTask
import com.github.gradle.node.yarn.task.YarnTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class NodePlugin : Plugin<Project> {
    private lateinit var project: Project
    private lateinit var nodeExtension: NodeExtension

    override fun apply(project: Project) {
        this.project = project
        this.nodeExtension = NodeExtension.create(project)
        addGlobalTypes()
        addTasks()
        addNpmRule()
        addYarnRule()
        this.project.afterEvaluate {
            nodeExtension.finalize()
        }
    }

    private fun addGlobalTypes() {
        addGlobalTaskType<NodeTask>()
        addGlobalTaskType<NpmTask>()
        addGlobalTaskType<NpxTask>()
        addGlobalTaskType<YarnTask>()
    }

    private inline fun <reified T> addGlobalTaskType() {
        project.extensions.extraProperties[T::class.java.simpleName] = T::class.java
    }

    private fun addTasks() {
        project.tasks.create<NpmInstallTask>(NpmInstallTask.NAME)
        project.tasks.create<YarnInstallTask>(YarnInstallTask.NAME)
        project.tasks.create<NodeSetupTask>(NodeSetupTask.NAME)
        project.tasks.create<NpmSetupTask>(NpmSetupTask.NAME)
        project.tasks.create<YarnSetupTask>(YarnSetupTask.NAME)
    }

    private fun addNpmRule() { // note this rule also makes it possible to specify e.g. "dependsOn npm_install"
        project.tasks.addRule("Pattern: \"npm_<command>\": Executes an NPM command.") {
            val taskName = this
            if (taskName.startsWith("npm_")) {
                val npmTask = project.tasks.create<NpmTask>(taskName)
                val tokens = taskName.split("_").drop(1) // all except first
                npmTask.npmCommand.set(tokens)
                if (tokens.first().equals("run", ignoreCase = true)) {
                    npmTask.dependsOn(NpmInstallTask.NAME)
                }
            }
        }
    }

    private fun addYarnRule() { // note this rule also makes it possible to specify e.g. "dependsOn yarn_install"
        project.tasks.addRule("Pattern: \"yarn_<command>\": Executes an Yarn command.") {
            val taskName = this
            if (taskName.startsWith("yarn_")) {
                val yarnTask = project.tasks.create<YarnTask>(taskName)
                val tokens = taskName.split("_").drop(1) // all except first
                yarnTask.yarnCommand.set(tokens.toMutableList())
                if (tokens.first().equals("run", ignoreCase = true)) {
                    yarnTask.dependsOn(YarnInstallTask.NAME)
                }
            }
        }
    }

    companion object {
        const val NODE_GROUP = "Node"
        const val NPM_GROUP = "npm"
        const val YARN_GROUP = "Yarn"
    }
}
