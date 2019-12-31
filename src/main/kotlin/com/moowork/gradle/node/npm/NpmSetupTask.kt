package com.moowork.gradle.node.npm

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.task.SetupTask
import com.moowork.gradle.node.util.Alias
import com.moowork.gradle.node.util.MutableAlias
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult
import java.util.*

/**
 * npm install that only gets executed if gradle decides so.
 */
open class NpmSetupTask : DefaultTask() {

    @get:Nested
    val execRunner = NpmExecRunner(project)

    @get:Input
    var args = mutableListOf<String>()
    @get:OutputDirectory
    val npmDir by Alias { variant::npmDir }

    @get:Internal
    val config by lazy { NodeExtension[project] }
    @get:Internal
    val variant by Alias { config::variant }
    @get:Internal
    var ignoreExitValue by MutableAlias { execRunner::ignoreExitValue }
    @get:Internal
    var execOverrides by MutableAlias { execRunner::execOverrides }
    @get:Internal
    var result: ExecResult? = null

    init {
        group = NodePlugin.NODE_GROUP
        description = "Setup a specific version of npm to be used by the build."
        dependsOn(SetupTask.NAME)
        isEnabled = false
    }

    @Input
    open fun getInput(): Set<Any?> {
        val set: MutableSet<Any?> = HashSet()
        set.add(config.download)
        set.add(config.npmVersion)
        set.add(config.npmWorkDir)
        return set
    }

    @TaskAction
    fun exec() {
        execRunner.arguments.addAll(args)
        result = execRunner.execute()
    }

    open fun configureVersion(version: String) {
        if (version.isNotEmpty()) {
            logger.debug("Setting npmVersion to {}", version)
            args.addAll(0, listOf("install", "--global", "--no-save", *PROXY_SETTINGS.toTypedArray(), "--prefix", variant.npmDir.absolutePath, "npm@$version"))
            isEnabled = true
        }
    }

    companion object {
        const val NAME = "npmSetup"

        val PROXY_SETTINGS by lazy {
            for ((proxyProto, proxyParam) in listOf(arrayOf("http", "--proxy"), arrayOf("https", "--https-proxy"))) {
                var proxyHost = System.getProperty("$proxyProto.proxyHost")
                val proxyPort = System.getProperty("$proxyProto.proxyPort")
                if (proxyHost != null && proxyPort != null) {
                    proxyHost = proxyHost.replace("^https?://".toRegex(), "")
                    val proxyUser = System.getProperty("$proxyProto.proxyUser")
                    val proxyPassword = System.getProperty("$proxyProto.proxyPassword")
                    return@lazy if (proxyUser != null && proxyPassword != null) {
                        listOf("$proxyParam $proxyProto://$proxyUser:$proxyPassword@$proxyHost:$proxyPort")
                    } else {
                        listOf("$proxyParam $proxyProto://$proxyHost:$proxyPort")
                    }
                }
            }
            return@lazy emptyList()
        }
    }
}
