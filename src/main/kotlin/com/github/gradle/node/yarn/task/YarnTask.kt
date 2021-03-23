package com.github.gradle.node.yarn.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.task.AbstractNodeExecTask
import com.github.gradle.node.yarn.exec.YarnExecRunner
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.process.ExecResult

abstract class YarnTask : AbstractNodeExecTask() {
    @get:Optional
    @get:Input
    val yarnCommand = objects.listProperty<String>()

    @get:Optional
    @get:Input
    val args = objects.listProperty<String>()

    init {
        group = NodePlugin.NODE_GROUP
        dependsOn(YarnSetupTask.NAME)
    }

    override fun execInternal(): ExecResult {
        val command = yarnCommand.get().plus(args.get())
        val nodeExecConfiguration =
            NodeExecConfiguration(
                command, environment.get(), workingDir.asFile.orNull,
                ignoreExitValue.get(), execOverrides.orNull
            )
        val yarnExecRunner = objects.newInstance(YarnExecRunner::class.java)
        return yarnExecRunner.executeYarnCommand(projectHelper, nodeExtension, nodeExecConfiguration)
    }
}
