package com.github.gradle.node.task

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.exec.NodeExecRunner
import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecSpec

open class NodeTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(RELATIVE)
    val script = project.objects.fileProperty()

    @get:Input
    val options = project.objects.listProperty<String>()

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
        dependsOn(NodeSetupTask.NAME)
    }

    // For Groovy DSL
    @Suppress("unused")
    fun setExecOverrides(execOverrides: Closure<ExecSpec>) {
        this.execOverrides.set { execOverrides.invoke(this) }
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
