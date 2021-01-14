package com.github.gradle.node.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.exec.NodeExecRunner
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecSpec

/**
 * Gradle task for running a Node.js script
 */
open class NodeTask : DefaultTask() {
    /**
     * Node.js script to run.
     */
    @get:InputFile
    @get:PathSensitive(RELATIVE)
    val script = project.objects.fileProperty()

    /**
     * Arguments to be passed to Node.js
     */
    @get:Input
    val options = project.objects.listProperty<String>()

    /**
     * Additional arguments for the [script] being run.
     */
    @get:Input
    val args = project.objects.listProperty<String>()

    /**
     * If enabled prevents the task from failing if the exit code is not 0. Defaults to false.
     */
    @get:Input
    val ignoreExitValue = project.objects.property<Boolean>().convention(false)

    /**
     * Sets the working directory.
     */
    @get:Internal
    val workingDir = project.objects.directoryProperty()

    /**
     * Add additional environment variables or override environment variables inherited from the system.
     */
    @get:Input
    val environment = project.objects.mapProperty<String, String>()

    /**
     * Overrides for [ExecSpec]
     */
    @get:Internal
    val execOverrides = project.objects.property<Action<ExecSpec>>()

    init {
        group = NodePlugin.NODE_GROUP
        dependsOn(NodeSetupTask.NAME)
    }

    // For DSL
    @Suppress("unused")
    fun execOverrides(execOverrides: Action<ExecSpec>) {
        this.execOverrides.set(execOverrides)
    }

    @TaskAction
    fun exec() {
        val currentScript = script.get().asFile
        val command = options.get().plus(currentScript.absolutePath).plus(args.get())
        val nodeExecConfiguration =
                NodeExecConfiguration(command, environment.get(), workingDir.asFile.orNull,
                        ignoreExitValue.get(), execOverrides.orNull)
        val nodeExecRunner = NodeExecRunner()
        nodeExecRunner.execute(project, nodeExecConfiguration)
    }
}
