package com.github.gradle.node

import org.gradle.api.Project
import java.io.File

open class NodeExtension(project: Project) {
    private val cacheDir = File(project.projectDir, ".gradle")
    var workDir = File(cacheDir, "nodejs")
    var npmWorkDir = File(cacheDir, "npm")
    var yarnWorkDir = File(cacheDir, "yarn")
    var nodeModulesDir: File = project.projectDir
    var version = "12.16.1"
    var npmVersion = ""
    var yarnVersion = ""
    var distBaseUrl: String? = "https://nodejs.org/dist"
    var npmCommand = "npm"
    var npxCommand = "npx"
    var npmInstallCommand = "install"
    var yarnCommand = "yarn"
    var download = false

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
