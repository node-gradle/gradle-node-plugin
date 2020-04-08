package com.github.gradle.node.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.exec.NodeExecRunner
import com.github.gradle.node.util.MutableAlias
import com.github.gradle.node.util.OverrideMapAlias
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.api.tasks.PathSensitivity.RELATIVE
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

    fun execOverrides(execOverrides: Action<ExecSpec>) {
        execRunner.execOverrides = execOverrides
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
