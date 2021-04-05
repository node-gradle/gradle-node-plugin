package com.github.gradle.node.npm.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecRunner
import com.github.gradle.node.task.AbstractNodeExecTask
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecResult

abstract class NpxTask : AbstractNodeExecTask() {
    @get:Input
    val command = objects.property<String>()

    @get:Input
    val args = objects.listProperty<String>()

    init {
        group = NodePlugin.NPM_GROUP
        dependsOn(NpmSetupTask.NAME)
    }

    override fun execInternal(): ExecResult {
        val fullCommand: MutableList<String> = mutableListOf()
        command.orNull?.let { fullCommand.add(it) }
        fullCommand.addAll(args.get())
        val nodeExecConfiguration =
            NodeExecConfiguration(fullCommand, environment.get(), workingDir.asFile.orNull, execOverrides.orNull)
        val npmExecRunner = objects.newInstance(NpmExecRunner::class.java)
        return npmExecRunner.executeNpxCommand(projectHelper, nodeExtension, nodeExecConfiguration)
    }
}
