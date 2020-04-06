package com.github.gradle.node

import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmSetupTask
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.task.NodeSetupTask
import com.github.gradle.node.task.NodeTask
import com.github.gradle.node.variant.VariantBuilder
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
            nodeExtension.variant = VariantBuilder(nodeExtension).build()
        }
    }

    private fun addGlobalTypes() {
        addGlobalTaskType(NodeTask::class.java)
        addGlobalTaskType(NpmTask::class.java)
        addGlobalTaskType(NpxTask::class.java)
        addGlobalTaskType(YarnTask::class.java)
    }

    private fun addGlobalTaskType(type: Class<*>) {
        project.extensions.extraProperties[type.simpleName] = type
    }

    private fun addTasks() {
        project.tasks.create(NpmInstallTask.NAME, NpmInstallTask::class)
        project.tasks.create(YarnInstallTask.NAME, YarnInstallTask::class)
        project.tasks.create(NodeSetupTask.NAME, NodeSetupTask::class)
        project.tasks.create(NpmSetupTask.NAME, NpmSetupTask::class)
        project.tasks.create(YarnSetupTask.NAME, YarnSetupTask::class)
    }

    private fun addNpmRule() { // note this rule also makes it possible to specify e.g. "dependsOn npm_install"
        project.tasks.addRule("Pattern: \"npm_<command>\": Executes an NPM command.") {
            val taskName = this
            if (taskName.startsWith("npm_")) {
                val npmTask = project.tasks.create(taskName, NpmTask::class)
                val tokens = taskName.split("_").drop(1) // all except first
                npmTask.npmCommand = tokens.toMutableList()
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
                val yarnTask = project.tasks.create(taskName, YarnTask::class)
                val tokens = taskName.split("_").drop(1) // all except first
                yarnTask.yarnCommand = tokens.toMutableList()
                if (tokens.first().equals("run", ignoreCase = true)) {
                    yarnTask.dependsOn(YarnInstallTask.NAME)
                }
            }
        }
    }

    companion object {
        const val NODE_GROUP = "Node"
    }
}
