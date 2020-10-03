package com.github.gradle.node

import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property

open class NodeExtension(project: Project) {
    private val cacheDir = project.layout.projectDirectory.dir(".gradle")
    val workDir = project.objects.directoryProperty().convention(cacheDir.dir("nodejs"))
    val npmWorkDir = project.objects.directoryProperty().convention(cacheDir.dir("npm"))
    val yarnWorkDir = project.objects.directoryProperty().convention(cacheDir.dir("yarn"))
    val nodeProjectDir = project.objects.directoryProperty().convention(project.layout.projectDirectory)
    val version = project.objects.property<String>().convention(DEFAULT_NODE_VERSION)
    val npmVersion = project.objects.property<String>().convention("")
    val yarnVersion = project.objects.property<String>().convention("")
    val distBaseUrl = project.objects.property<String>()
    val npmCommand = project.objects.property<String>().convention("npm")
    val npxCommand = project.objects.property<String>().convention("npx")
    val npmInstallCommand = project.objects.property<String>().convention("install")
    val yarnCommand = project.objects.property<String>().convention("yarn")
    val download = project.objects.property<Boolean>().convention(false)
    val disableTaskRules = project.objects.property<Boolean>().convention(true)
    val useGradleProxySettings = project.objects.property<Boolean>().convention(true)

    @Suppress("unused")
    @Deprecated("Deprecated in version 3.0, please use nodeProjectDir now")
    val nodeModulesDir = nodeProjectDir

    init {
        distBaseUrl.set("https://nodejs.org/dist")
    }

    companion object {
        const val NAME = "node"
        const val DEFAULT_NODE_VERSION = "12.18.1"
        const val DEFAULT_NPM_VERSION = "6.14.5"

        @JvmStatic
        operator fun get(project: Project): NodeExtension {
            return project.extensions.getByType()
        }

        @JvmStatic
        fun create(project: Project): NodeExtension {
            return project.extensions.create(NAME, project)
        }
    }
}
