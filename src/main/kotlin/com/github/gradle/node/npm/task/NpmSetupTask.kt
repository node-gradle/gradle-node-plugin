package com.github.gradle.node.npm.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecRunner
import com.github.gradle.node.task.NodeSetupTask
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecSpec
import java.util.*

/**
 * npm install that only gets executed if gradle decides so.
 */
open class NpmSetupTask : DefaultTask() {
    @get:Internal
    protected val nodeExtension by lazy { NodeExtension[project] }

    @get:Input
    val args = project.objects.listProperty<String>()

    @get:Input
    val ignoreExitValue = project.objects.property<Boolean>().convention(false)

    @get:Internal
    val execOverrides = project.objects.property<Action<ExecSpec>>()

    @get:Input
    val download by lazy { nodeExtension.download }

    @get:OutputDirectory
    val npmDir by lazy {
        val variantComputer = VariantComputer()
        val nodeDir = variantComputer.computeNodeDir(nodeExtension)
        variantComputer.computeNpmDir(nodeExtension, nodeDir)
    }

    init {
        group = NodePlugin.NODE_GROUP
        description = "Setup a specific version of npm to be used by the build."
        dependsOn(NodeSetupTask.NAME)
        project.afterEvaluate {
            isEnabled = isTaskEnabled()
        }
    }

    @Input
    open fun getInput(): Provider<Set<String>> {
        return zip(nodeExtension.npmVersion, nodeExtension.npmWorkDir)
                .map { (npmVersion, npmWorkingDir) ->
                    setOf(npmVersion, npmWorkingDir.asFile.toString())
                }
    }

    // For DSL
    @Suppress("unused")
    fun execOverrides(execOverrides: Action<ExecSpec>) {
        this.execOverrides.set(execOverrides)
    }

    @TaskAction
    fun exec() {
        val command = computeCommand()
        val nodeExecConfiguration = NodeExecConfiguration(command, ignoreExitValue = ignoreExitValue.get(),
                execOverrides = execOverrides.orNull)
        val npmExecRunner = NpmExecRunner()
        npmExecRunner.executeNpmCommand(project, nodeExecConfiguration)
    }

    protected open fun computeCommand(): List<String> {
        val version = nodeExtension.npmVersion.get()
        return listOf("install", "--global", "--no-save", *PROXY_SETTINGS.toTypedArray(), "--prefix",
                npmDir.get().asFile.absolutePath, "npm@$version") + args.get()
    }

    @Internal
    protected open fun isTaskEnabled(): Boolean {
        return nodeExtension.npmVersion.get().isNotBlank()
    }

    companion object {
        const val NAME = "npmSetup"

        val PROXY_SETTINGS by lazy {
            val proxyArgs = ArrayList<String>()
            for ((proxyProto, proxyParam) in listOf(arrayOf("http", "--proxy"), arrayOf("https", "--https-proxy"))) {
                var proxyHost = System.getProperty("$proxyProto.proxyHost")
                val proxyPort = System.getProperty("$proxyProto.proxyPort")
                if (proxyHost != null && proxyPort != null) {
                    proxyHost = proxyHost.replace("^https?://".toRegex(), "")
                    val proxyUser = System.getProperty("$proxyProto.proxyUser")
                    val proxyPassword = System.getProperty("$proxyProto.proxyPassword")
                    if (proxyUser != null && proxyPassword != null) {
                        proxyArgs.add("$proxyParam $proxyProto://$proxyUser:$proxyPassword@$proxyHost:$proxyPort")
                    } else {
                        proxyArgs.add("$proxyParam $proxyProto://$proxyHost:$proxyPort")
                    }
                }
            }
            return@lazy proxyArgs.toList()
        }
    }
}
