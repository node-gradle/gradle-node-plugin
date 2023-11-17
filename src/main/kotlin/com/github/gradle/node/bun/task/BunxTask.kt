package com.github.gradle.node.bun.task

import com.github.gradle.node.bun.exec.BunExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import javax.inject.Inject

abstract class BunxTask : BunAbstractTask() {
    @get:Input
    val command = objects.property<String>()

    @TaskAction
    fun exec() {
        val command = command.map { listOf(it) }.get().plus(args.get())
        val nodeExecConfiguration =
            NodeExecConfiguration(
                command, environment.get(), workingDir.asFile.orNull,
                ignoreExitValue.get(), execOverrides.orNull
            )
        val bunExecRunner = objects.newInstance(BunExecRunner::class.java)
        result = bunExecRunner.executeBunxCommand(nodeExtension, nodeExecConfiguration, variantComputer)
    }
}
