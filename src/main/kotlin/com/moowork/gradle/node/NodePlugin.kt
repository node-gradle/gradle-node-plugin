package com.moowork.gradle.node

import com.moowork.gradle.node.npm.NpmInstallTask
import com.moowork.gradle.node.npm.NpmSetupTask
import com.moowork.gradle.node.npm.NpmTask
import com.moowork.gradle.node.npm.NpxTask
import com.moowork.gradle.node.task.NodeTask
import com.moowork.gradle.node.task.SetupTask
import com.moowork.gradle.node.variant.VariantBuilder
import com.moowork.gradle.node.yarn.YarnInstallTask
import com.moowork.gradle.node.yarn.YarnSetupTask
import com.moowork.gradle.node.yarn.YarnTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class NodePlugin : Plugin<Project> {

    private lateinit var project: Project
    private lateinit var config: NodeExtension

    private lateinit var setupTask: SetupTask
    private lateinit var npmSetupTask: NpmSetupTask
    private lateinit var yarnSetupTask: YarnSetupTask

    override fun apply(project: Project) {
        this.project = project
        this.config = NodeExtension.create(project)
        addGlobalTypes()
        addTasks()
        addNpmRule()
        addYarnRule()
        this.project.afterEvaluate {
            config.variant = VariantBuilder(config).build()
            configureSetupTask()
            configureNpmSetupTask()
            configureYarnSetupTask()
        }
    }

    private fun addGlobalTypes() {
        addGlobalTaskType(NodeTask::class.java)
        addGlobalTaskType(NpmTask::class.java)
        addGlobalTaskType(NpxTask::class.java)
        addGlobalTaskType(YarnTask::class.java)
    }

    private fun addTasks() {
        project.tasks.create(NpmInstallTask.NAME, NpmInstallTask::class)
        project.tasks.create(YarnInstallTask.NAME, YarnInstallTask::class)
        setupTask = project.tasks.create(SetupTask.NAME, SetupTask::class)
        npmSetupTask = project.tasks.create(NpmSetupTask.NAME, NpmSetupTask::class)
        yarnSetupTask = project.tasks.create(YarnSetupTask.NAME, YarnSetupTask::class)
    }

    private fun addGlobalTaskType(type: Class<*>) {
        project.extensions.extraProperties[type.simpleName] = type
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

    private fun configureSetupTask() {
        setupTask.isEnabled = config.download
    }

    private fun configureNpmSetupTask() {
        npmSetupTask.configureVersion(config.npmVersion)
    }

    private fun configureYarnSetupTask() {
        yarnSetupTask.configureVersion(config.yarnVersion)
    }

    companion object {
        const val NODE_GROUP = "Node"
    }
}
