package com.moowork.gradle.node.yarn

import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.npm.NpmSetupTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import java.io.File
import java.util.*

/**
 * Setup a specific version of Yarn to be used by the build.
 */
open class YarnSetupTask : NpmSetupTask() {

    init {
        group = NodePlugin.NODE_GROUP
        description = "Setup a specific version of Yarn to be used by the build."
        isEnabled = false
    }

    @Input
    override fun getInput(): Set<Any?> {
        val set: MutableSet<Any?> = HashSet()
        set.add(config.download)
        set.add(config.yarnVersion)
        return set
    }

    @OutputDirectory
    fun getYarnDir(): File {
        return variant.yarnDir
    }

    override fun configureVersion(version: String) {
        var pkg = "yarn"
        if (version.isNotEmpty()) {
            logger.debug("Setting yarnVersion to $version")
            pkg += "@$version"
        }
        args = listOf("install", "--global", "--no-save", *PROXY_SETTINGS.toTypedArray(), "--prefix", variant.yarnDir.absolutePath, pkg) + args
        isEnabled = true
    }

    companion object {
        const val NAME = "yarnSetup"
    }
}
