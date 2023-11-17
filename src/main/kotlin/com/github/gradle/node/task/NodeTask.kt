package com.github.gradle.node.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.exec.NodeExecRunner
import com.github.gradle.node.util.DefaultProjectApiHelper
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import javax.inject.Inject

/**
 * Gradle task for running a Node.js script
 */
abstract class NodeTask : BaseTask() {
    @get:Inject
    abstract val objects: ObjectFactory

    @get:Inject
    abstract val providers: ProviderFactory

    @get:Inject
    abstract val execOperations: ExecOperations

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

    /**
     * If enabled prevents the task from failing if the exit code is not 0. Defaults to false.
     */
    @get:Input
    val ignoreExitValue = objects.property<Boolean>().convention(false)

    /**
     * Sets the working directory.
     */
    @get:Internal
    val workingDir = objects.directoryProperty()

    /**
     * Add additional environment variables or override environment variables inherited from the system.
     */
    @get:Input
    val environment = objects.mapProperty<String, String>()

    @get:Internal
    val execOverrides = objects.property<Action<ExecSpec>>()

    /**
     * Overrides for [ExecSpec]
     */
    @get:Internal
    val extension = NodeExtension[project]

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
        result = nodeExecRunner.execute(execOperations, extension, nodeExecConfiguration, variantComputer)
    }
}
