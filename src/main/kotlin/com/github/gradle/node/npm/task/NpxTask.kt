package com.github.gradle.node.npm.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecRunner
import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecSpec

open class NpxTask : DefaultTask() {
    @get:Input
    val command = project.objects.property<String>()

    @get:Input
    val args = project.objects.listProperty<String>()

    @get:Input
    val ignoreExitValue = project.objects.property<Boolean>().convention(false)

    @get:Internal
    val workingDir = project.objects.directoryProperty()

    @get:Input
    val environment = project.objects.mapProperty<String, String>()

    @get:Internal
    val execOverrides = project.objects.property<(ExecSpec.() -> Unit)>()

    init {
        group = NodePlugin.NODE_GROUP
        dependsOn(NpmSetupTask.NAME)
    }

    // For Groovy DSL
    @Suppress("unused")
    fun setExecOverrides(execOverrides: Closure<ExecSpec>) {
        this.execOverrides.set { execOverrides.invoke(this) }
    }

    @TaskAction
    fun exec() {
        val fullCommand: MutableList<String> = mutableListOf()
        command.orNull?.let { fullCommand.add(it) }
        fullCommand.addAll(args.get())
        val nodeExecConfiguration =
                NodeExecConfiguration(fullCommand, environment.get(), workingDir.asFile.orNull,
                        ignoreExitValue.get(), execOverrides.orNull)
        val npmExecRunner = NpmExecRunner()
        npmExecRunner.executeNpxCommand(project, nodeExecConfiguration)
    }
}
