package com.github.gradle.node.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.exec.NodeExecRunner
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.kotlin.dsl.listProperty
import org.gradle.process.ExecResult

/**
 * Gradle task for running a Node.js script
 */
abstract class NodeTask : AbstractNodeExecTask() {
    /**
     * Node.js script to run.
     */
    @get:InputFile
    @get:PathSensitive(RELATIVE)
    val script = objects.fileProperty()

    /**
     * Arguments to be passed to Node.js
     */
    @get:Input
    val options = objects.listProperty<String>()

    /**
     * Additional arguments for the [script] being run.
     */
    @get:Input
    val args = objects.listProperty<String>()

    init {
        group = NodePlugin.NODE_GROUP
        dependsOn(NodeSetupTask.NAME)
    }

    override fun execInternal(): ExecResult {
        val currentScript = script.get().asFile
        val command = options.get().plus(currentScript.absolutePath).plus(args.get())
        val nodeExecConfiguration =
            NodeExecConfiguration(command, environment.get(), workingDir.asFile.orNull, execOverrides.orNull)
        val nodeExecRunner = NodeExecRunner()
        return nodeExecRunner.execute(projectHelper, nodeExtension, nodeExecConfiguration)
    }
}
