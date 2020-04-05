package com.github.gradle.node

import com.github.gradle.node.variant.Variant
import org.gradle.api.Project
import java.io.File
import kotlin.properties.Delegates

@Suppress("MemberVisibilityCanBePrivate", "unused") // Extension object; properties may be configured in build scripts
open class NodeExtension(project: Project) {

    private val cacheDir = File(project.projectDir, ".gradle")
    var workDir = File(cacheDir, "nodejs")
    var npmWorkDir = File(cacheDir, "npm")
    var yarnWorkDir = File(cacheDir, "yarn")
    var nodeModulesDir: File = project.projectDir
    var version = "10.14.0"
    var npmVersion = ""
    var yarnVersion = ""
    var distBaseUrl: String? = "https://nodejs.org/dist"
    var npmCommand = "npm"
    var npxCommand = "npx"
    var npmInstallCommand = "install"
    var yarnCommand = "yarn"
    var download = false
    var variant by Delegates.notNull<Variant>()

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
