package com.github.gradle.node

import org.gradle.api.Project
import org.gradle.kotlin.dsl.property

open class NodeExtension(project: Project) {
    private val cacheDir = project.layout.projectDirectory.dir(".gradle")
    val workDir = project.objects.directoryProperty().convention(cacheDir.dir("nodejs"))
    val npmWorkDir = project.objects.directoryProperty().convention(cacheDir.dir("npm"))
    val yarnWorkDir = project.objects.directoryProperty().convention(cacheDir.dir("yarn"))
    val nodeModulesDir = project.objects.directoryProperty().convention(project.layout.projectDirectory)
    val version = project.objects.property<String>().convention("12.16.2")
    val npmVersion = project.objects.property<String>().convention("")
    val yarnVersion = project.objects.property<String>().convention("")
    val distBaseUrl = project.objects.property<String?>()
    val npmCommand = project.objects.property<String>().convention("npm")
    val npxCommand = project.objects.property<String>().convention("npx")
    val npmInstallCommand = project.objects.property<String>().convention("install")
    val yarnCommand = project.objects.property<String>().convention("yarn")
    val download = project.objects.property<Boolean>().convention(false)

    init {
        distBaseUrl.set("https://nodejs.org/dist")
    }

    internal fun finalize() {
        workDir.finalizeValue()
        npmWorkDir.finalizeValue()
        yarnWorkDir.finalizeValue()
        nodeModulesDir.finalizeValue()
        version.finalizeValue()
        npmVersion.finalizeValue()
        yarnVersion.finalizeValue()
        distBaseUrl.finalizeValue()
        npmCommand.finalizeValue()
        npxCommand.finalizeValue()
        npmInstallCommand.finalizeValue()
        yarnCommand.finalizeValue()
        download.finalizeValue()
    }

    companion object {
        const val NAME = "node"

        @JvmStatic
        operator fun get(project: Project): NodeExtension {
            return project.extensions.getByType(NodeExtension::class.java)
        }

        @JvmStatic
        fun create(project: Project): NodeExtension {
            return project.extensions.create(NAME, NodeExtension::class.java, project)
        }
    }
}
