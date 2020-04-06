package com.github.gradle.node.npm.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecRunner
import com.github.gradle.node.task.NodeSetupTask
import com.github.gradle.node.util.Alias
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecSpec
import java.util.*

/**
 * npm install that only gets executed if gradle decides so.
 */
open class NpmSetupTask : DefaultTask() {
    @get:Internal
    protected val nodeExtension by lazy { NodeExtension[project] }
    @get:Internal
    protected val variant by Alias { nodeExtension::variant }
    @get:Input
    var args = listOf<String>()
    @get:Input
    var ignoreExitValue = false
    @get:Internal
    var execOverrides: (ExecSpec.() -> Unit)? = null
    @get:OutputDirectory
    val npmDir by Alias { variant::npmDir }

    init {
        group = NodePlugin.NODE_GROUP
        description = "Setup a specific version of npm to be used by the build."
        dependsOn(NodeSetupTask.NAME)
        project.afterEvaluate {
            isEnabled = isTaskEnabled()
        }
    }

    @Input
    open fun getInput(): Set<Any?> {
        val set: MutableSet<Any?> = HashSet()
        set.add(nodeExtension.download)
        set.add(nodeExtension.npmVersion)
        set.add(nodeExtension.npmWorkDir)
        return set
    }

    @TaskAction
    fun exec() {
        val command = computeCommand()
        val nodeExecConfiguration = NodeExecConfiguration(command, ignoreExitValue = ignoreExitValue,
                execOverrides = execOverrides)
        val npmExecRunner = NpmExecRunner()
        npmExecRunner.executeNpmCommand(project, nodeExecConfiguration)
    }

    protected open fun computeCommand(): List<String> {
        val version = nodeExtension.npmVersion
        return listOf("install", "--global", "--no-save", *PROXY_SETTINGS.toTypedArray(), "--prefix",
                npmDir.absolutePath, "npm@$version") + args
    }

    @Internal
    protected open fun isTaskEnabled(): Boolean {
        return nodeExtension.npmVersion.isNotEmpty()
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
