package com.github.gradle.node.bun.task

import com.github.gradle.node.bun.exec.BunExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty

abstract class BunTask : BunAbstractTask() {
    @get:Optional
    @get:Input
    val bunCommand = objects.listProperty<String>()

    @TaskAction
    fun exec() {
        val command = bunCommand.get().plus(args.get())
        val nodeExecConfiguration =
            NodeExecConfiguration(
                command, environment.get(), workingDir.asFile.orNull,
                ignoreExitValue.get(), execOverrides.orNull
            )
        val bunExecRunner = objects.newInstance(BunExecRunner::class.java)
        result = bunExecRunner.executeBunCommand(execOperations, nodeExtension, nodeExecConfiguration, variantComputer)
    }
}
