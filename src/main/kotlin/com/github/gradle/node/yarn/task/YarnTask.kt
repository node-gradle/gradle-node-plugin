package com.github.gradle.node.yarn.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.yarn.exec.YarnExecRunner
import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.invoke
import org.gradle.process.ExecSpec
import java.io.File

open class YarnTask : DefaultTask() {
    @get:Optional
    @get:Input
    var yarnCommand = listOf<String>()
    @get:Optional
    @get:Input
    var args = listOf<String>()
    @get:Input
    var ignoreExitValue = false
    @get:Internal
    var workingDir: File? = null
    @get:Input
    var environment: Map<String, String> = mapOf()
    @get:Internal
    var execOverrides: (ExecSpec.() -> Unit)? = null

    init {
        group = NodePlugin.NODE_GROUP
        dependsOn(YarnSetupTask.NAME)
    }

    // For Groovy DSL
    @Suppress("unused")
    fun setExecOverrides(execOverrides: Closure<ExecSpec>) {
        this.execOverrides = { execOverrides.invoke(this) }
    }

    @TaskAction
    fun exec() {
        val command = yarnCommand.plus(args)
        val nodeExecConfiguration =
                NodeExecConfiguration(command, environment, workingDir, ignoreExitValue, execOverrides)
        val yarnExecRunner = YarnExecRunner()
        yarnExecRunner.executeYarnCommand(project, nodeExecConfiguration)
    }
}
