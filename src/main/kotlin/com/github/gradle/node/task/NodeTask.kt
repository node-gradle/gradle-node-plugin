package com.github.gradle.node.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.exec.NodeExecRunner
import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.kotlin.dsl.invoke
import org.gradle.process.ExecSpec
import java.io.File

open class NodeTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(RELATIVE)
    var script: File? = null

    @get:Input
    var options = listOf<String>()

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
        dependsOn(NodeSetupTask.NAME)
    }

    // For Groovy DSL
    @Suppress("unused")
    fun setExecOverrides(execOverrides: Closure<ExecSpec>) {
        this.execOverrides = { execOverrides.invoke(this) }
    }

    @TaskAction
    fun exec() {
        val currentScript = checkNotNull(script) { "Required script property is not set." }
        val command = options.plus(currentScript.absolutePath).plus(args)
        val nodeExecConfiguration =
                NodeExecConfiguration(command, environment, workingDir, ignoreExitValue, execOverrides)
        val nodeExecRunner = NodeExecRunner()
        nodeExecRunner.execute(project, nodeExecConfiguration)
    }
}
