package com.github.gradle.node.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.ProjectApiHelper
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import javax.inject.Inject

abstract class AbstractNodeExecTask : DefaultTask() {
    @get:Inject
    abstract val objects: ObjectFactory

    @get:Internal
    val projectHelper = ProjectApiHelper.newInstance(project)

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

    @get:Internal
    val nodeExtension = NodeExtension[project]

    // For DSL
    @Suppress("unused")
    fun execOverrides(execOverrides: Action<ExecSpec>) {
        this.execOverrides.set(execOverrides)
    }

    @get:Internal
    val onSuccess = objects.property<Action<ExecResult>>()

    // For DSL
    @Suppress("unused")
    fun onSuccess(onSuccess: Action<ExecResult>) {
        this.onSuccess.set(onSuccess)
    }

    @get:Internal
    val onFailure = objects.property<Action<Exception>>()

    // For DSL
    @Suppress("unused")
    fun onFailure(onFailure: Action<Exception>) {
        this.onFailure.set(onFailure)
    }

    @TaskAction
    fun exec() {
        try {
            val execResult = execInternal()
            onSuccess.orNull?.execute(execResult)
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure.orNull?.execute(e)
            throw e
        }
    }

    abstract fun execInternal(): ExecResult;
}
