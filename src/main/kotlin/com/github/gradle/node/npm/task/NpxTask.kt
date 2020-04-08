package com.github.gradle.node.npm.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecRunner
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec
import java.io.File

open class NpxTask : DefaultTask() {
    @get:Input
    var command: String? = null
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
        dependsOn(NpmSetupTask.NAME)
    }

    @TaskAction
    fun exec() {
        val fullCommand: MutableList<String> = mutableListOf()
        command?.let { fullCommand.add(it) }
        fullCommand.addAll(args)
        val nodeExecConfiguration =
                NodeExecConfiguration(fullCommand, environment, workingDir, ignoreExitValue, execOverrides)
        val npmExecRunner = NpmExecRunner()
        npmExecRunner.executeNpxCommand(project, nodeExecConfiguration)
    }
}
