package com.github.gradle.node.npm.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.npm.exec.NpmExecSource
import com.github.gradle.node.task.BaseTask
import com.github.gradle.node.util.DefaultProjectApiHelper
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.*
import org.gradle.process.ExecSpec
import javax.inject.Inject

abstract class NpmTask : BaseTask() {
    @get:Inject
    abstract val objects: ObjectFactory

    @get:Inject
    abstract val providers: ProviderFactory

    @get:Optional
    @get:Input
    val npmCommand = objects.listProperty<String>()

    @get:Optional
    @get:Input
    val args = objects.listProperty<String>()

    @get:Input
    val ignoreExitValue = objects.property<Boolean>().convention(false)

    @get:Internal
    val workingDir = objects.fileProperty()

    @get:Input
    val environment = objects.mapProperty<String, String>()

    @get:Internal
    val execOverrides = objects.property<Action<ExecSpec>>()

    @get:Internal
    val projectHelper = project.objects.newInstance<DefaultProjectApiHelper>()

    @get:Internal
    val nodeExtension = NodeExtension[project]

    init {
        group = NodePlugin.NPM_GROUP
        dependsOn(NpmSetupTask.NAME)
    }

    // For DSL
    @Suppress("unused")
    fun execOverrides(execOverrides: Action<ExecSpec>) {
        this.execOverrides.set(execOverrides)
    }

    @TaskAction
    fun exec() {
        @Suppress("UnstableApiUsage")
        val npmExec = providers.of(NpmExecSource::class) {
            parameters.arguments.set(args)
            parameters.environment.set(environment)
//                parameters.includeSystemEnvironment.set(nodeExtension.includeSystemEnvironment)
//            parameters.additionalBinPaths.set(nodeExtension.additionalBinPaths)
            parameters.download.set(nodeExtension.download)
            parameters.resolvedNodeDir.set(nodeExtension.resolvedNodeDir)
            parameters.resolvedPlatform.set(nodeExtension.resolvedPlatform)
            parameters.npmVersion.set(nodeExtension.npmVersion)
            parameters.npmCommand.set(listOf(nodeExtension.npmCommand.get()))
            parameters.npmWorkDir.set(nodeExtension.npmWorkDir)
            parameters.nodeProjectDir.set(nodeExtension.nodeProjectDir)
            parameters.nodeProxySettings.set(nodeExtension.nodeProxySettings)
//            parameters.executable.set(nodeExtension.executable)
            parameters.ignoreExitValue.set(true)
            parameters.workingDir.set(workingDir.asFile.orNull)
//            parameters.npmCommand.set(nodeExtension.npmCommand)
//            parameters.args.set(args)
        }
        val result = npmExec.get()
        if (result.failure != null) {
            logger.error(result.capturedOutput)
            throw RuntimeException("$path failed to execute npm command.", result.failure)
        }
        this.result = result.asExecResult()
//        val command = npmCommand.get().plus(args.get())
//        val nodeExecConfiguration =
//            NodeExecConfiguration(
//                command = command,
//                environment = environment.get(),
//                workingDir = workingDir.asFile.orNull,
//                ignoreExitValue = ignoreExitValue.get(),
//                execOverrides = execOverrides.orNull,
//            )
//        val npmExecRunner = objects.newInstance(NpmExecRunner::class.java)
//        result = npmExecRunner.executeNpmCommand(
//            project = projectHelper,
//            extension = nodeExtension,
//            nodeExecConfiguration = nodeExecConfiguration,
//            variants = variantComputer,
//        )
    }
}
