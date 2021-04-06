package com.github.gradle.node.pnpm.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.pnpm.exec.PnpmExecRunner
import com.github.gradle.node.util.ProjectApiHelper
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecSpec
import javax.inject.Inject

abstract class PnpxTask : DefaultTask() {
    @get:Inject
    abstract val objects: ObjectFactory

    @get:Input
    val command = objects.property<String>()

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
    val projectHelper = ProjectApiHelper.newInstance(project)

    @get:Internal
    val extension = NodeExtension[project]

    init {
        group = NodePlugin.PNPM_GROUP
        dependsOn(PnpmSetupTask.NAME)
    }

    // For DSL
    @Suppress("unused")
    fun execOverrides(execOverrides: Action<ExecSpec>) {
        this.execOverrides.set(execOverrides)
    }

    @TaskAction
    fun exec() {
        val fullCommand: MutableList<String> = mutableListOf()
        command.orNull?.let { fullCommand.add(it) }
        fullCommand.addAll(args.get())
        val nodeExecConfiguration =
            NodeExecConfiguration(
                fullCommand, environment.get(), workingDir.asFile.orNull,
                ignoreExitValue.get(), execOverrides.orNull
            )
        val pnpmExecRunner = objects.newInstance(PnpmExecRunner::class.java)
        pnpmExecRunner.executePnpxCommand(projectHelper, extension, nodeExecConfiguration)
    }
}
