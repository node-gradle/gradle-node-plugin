package com.github.gradle.node.bun.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.bun.exec.BunExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.task.BaseTask
import com.github.gradle.node.util.DefaultProjectApiHelper
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecSpec
import javax.inject.Inject

abstract class BunTask : BaseTask() {
    @get:Inject
    abstract val objects: ObjectFactory

    @get:Inject
    abstract val providers: ProviderFactory

    @get:Optional
    @get:Input
    val bunCommand = objects.listProperty<String>()

    @get:Optional
    @get:Input
    val args = objects.listProperty<String>()

    @get:Input
    val ignoreExitValue = objects.property<Boolean>().convention(false)

    @get:Internal
    val workingDir = objects.directoryProperty()

    @get:Input
    val environment = objects.mapProperty<String, String>()

    @get:Internal
    val execOverrides = objects.property<Action<ExecSpec>>()

    @get:Internal
    val projectHelper = project.objects.newInstance<DefaultProjectApiHelper>()

    @get:Internal
    val nodeExtension = NodeExtension[project]

    init {
        group = NodePlugin.BUN_GROUP
        dependsOn(BunSetupTask.NAME)
    }

    // For DSL
    @Suppress("unused")
    fun execOverrides(execOverrides: Action<ExecSpec>) {
        this.execOverrides.set(execOverrides)
    }

    @TaskAction
    fun exec() {
        val command = bunCommand.get().plus(args.get())
        val nodeExecConfiguration =
            NodeExecConfiguration(
                command, environment.get(), workingDir.asFile.orNull,
                ignoreExitValue.get(), execOverrides.orNull
            )
        val bunExecRunner = objects.newInstance(BunExecRunner::class.java)
        result = bunExecRunner.executeBunCommand(projectHelper, nodeExtension, nodeExecConfiguration, variantComputer)
    }
}
