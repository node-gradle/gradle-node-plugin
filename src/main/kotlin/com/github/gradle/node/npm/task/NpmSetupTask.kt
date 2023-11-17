package com.github.gradle.node.npm.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.experiment.PackageManager
import com.github.gradle.node.npm.exec.NpmExecRunner
import com.github.gradle.node.task.BaseTask
import com.github.gradle.node.task.NodeSetupTask
import com.github.gradle.node.util.DefaultProjectApiHelper
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.newInstance
import java.io.File
import javax.inject.Inject

/**
 * npm install that only gets executed if gradle decides so.
 */
abstract class NpmSetupTask : BaseTask() {

    @get:Nested
    abstract val packageManager: Property<PackageManager>

    @get:Inject
    abstract val objects: ObjectFactory

    @get:Inject
    abstract val providers: ProviderFactory

    @get:Internal
    protected val nodeExtension = NodeExtension[project]

    @get:Internal
    val projectHelper = project.objects.newInstance<DefaultProjectApiHelper>()

    @get:Input
    val args = objects.listProperty<String>()

    @get:Input
    val download = nodeExtension.download

    @get:OutputDirectory
    val npmDir by lazy {
        val nodeDir = nodeExtension.resolvedNodeDir
        variantComputer.computeNpmDir(nodeExtension, nodeDir)
    }

    init {
        group = NodePlugin.NPM_GROUP
        description = "Setup a specific version of npm to be used by the build."
        dependsOn(NodeSetupTask.NAME)
        onlyIf {
            isTaskEnabled()
        }
    }

    @get:Input
    abstract val version: Property<String>

    @Internal
    open fun isTaskEnabled(): Boolean {
        return version.get().isNotBlank()
    }

    @TaskAction
    fun exec() {
        val command = computeCommand()
        val nodeExecConfiguration = NodeExecConfiguration(command)
        val npmExecRunner = objects.newInstance(NpmExecRunner::class.java)
        result = npmExecRunner.executeNpmCommand(projectHelper, nodeExtension, nodeExecConfiguration, variantComputer)
    }

    protected open fun computeCommand(): List<String> {
        val version = version.get()
        val directory = npmDir.get().asFile
        // npm < 7 creates the directory if it's missing, >= 7 fails if it's missing
        File(directory, "lib").mkdirs()
        return listOf("install", "--global", "--no-save", "--prefix", directory.absolutePath,
                "npm@$version") + args.get()
    }

    fun manager(manager: PackageManager) {
        this.packageManager.set(manager)
    }

    companion object {
        const val NAME = "npmSetup"
    }
}
