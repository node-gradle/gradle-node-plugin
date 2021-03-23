package com.github.gradle.node.npm.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecRunner
import com.github.gradle.node.task.AbstractNodeExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.process.ExecResult

abstract class NpmTask : AbstractNodeExecTask() {
    @get:Optional
    @get:Input
    val npmCommand = objects.listProperty<String>()

    @get:Optional
    @get:Input
    val args = objects.listProperty<String>()

    init {
        group = NodePlugin.NPM_GROUP
        dependsOn(NpmSetupTask.NAME)
    }

    override fun execInternal(): ExecResult {
        val command = npmCommand.get().plus(args.get())
        val nodeExecConfiguration =
            NodeExecConfiguration(
                command, environment.get(), workingDir.asFile.orNull, ignoreExitValue.get(),
                execOverrides.orNull
            )
        val npmExecRunner = objects.newInstance(NpmExecRunner::class.java)
        return npmExecRunner.executeNpmCommand(projectHelper, nodeExtension, nodeExecConfiguration)
    }
}
