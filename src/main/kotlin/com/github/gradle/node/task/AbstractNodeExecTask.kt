package com.github.gradle.node.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.task.TaskHooks
import com.github.gradle.node.npm.task.internal.TaskHooksImpl
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
import org.gradle.process.internal.ExecException
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
    val execOverrides = objects.property<Action<in ExecSpec>>()

    @get:Internal
    val nodeExtension = NodeExtension[project]

    // For DSL
    @Suppress("unused")
    fun execOverrides(execOverrides: Action<in ExecSpec>) {
        this.execOverrides.set(execOverrides)
    }

    @get:Internal
    val hooks = objects.property<Action<in TaskHooks>>()

    // For DSL
    @Suppress("unused")
    fun hooks(hooks: Action<in TaskHooks>) {
        this.hooks.set(hooks)
    }

    @TaskAction
    fun exec() {
        val taskHooks = TaskHooksImpl()
        hooks.orNull?.execute(taskHooks)
        val execResult = execInternal()
        if (!ignoreExitValue.get()) {
            try {
                execResult.assertNormalExitValue()
                taskHooks.successHandler?.execute(execResult)
            } catch (e: ExecException) {
                taskHooks.failureHandler?.execute(execResult)
                throw e
            }
        } else {
            taskHooks.successHandler?.execute(execResult)
        }
    }

    abstract fun execInternal(): ExecResult;
}
