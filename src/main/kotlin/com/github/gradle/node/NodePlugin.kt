package com.github.gradle.node

import com.github.gradle.node.npm.proxy.ProxySettings
import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmSetupTask
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.task.NodeSetupTask
import com.github.gradle.node.task.NodeTask
import com.github.gradle.node.variant.VariantComputer
import com.github.gradle.node.yarn.task.YarnInstallTask
import com.github.gradle.node.yarn.task.YarnSetupTask
import com.github.gradle.node.yarn.task.YarnTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.util.GradleVersion
import java.io.File

class NodePlugin : Plugin<Project> {
    private lateinit var project: Project

    override fun apply(project: Project) {
        this.project = project
        val nodeExtension = NodeExtension.create(project)
        addGlobalTypes()
        addTasks()
        addNpmRule()
        addYarnRule()
        project.afterEvaluate {
            if (nodeExtension.download.get()) {
                nodeExtension.distBaseUrl.orNull?.let { addRepository(it, nodeExtension.allowInsecureProtocol.orNull) }
                configureNodeSetupTask(nodeExtension)
            }
        }
    }

    private fun addGlobalTypes() {
        addGlobalType<NodeTask>()
        addGlobalType<NpmTask>()
        addGlobalType<NpxTask>()
        addGlobalType<YarnTask>()
        addGlobalType<ProxySettings>()
    }

    private inline fun <reified T> addGlobalType() {
        project.extensions.extraProperties[T::class.java.simpleName] = T::class.java
    }

    private fun addTasks() {
        project.tasks.register<NpmInstallTask>(NpmInstallTask.NAME)
        project.tasks.register<YarnInstallTask>(YarnInstallTask.NAME)
        project.tasks.register<NodeSetupTask>(NodeSetupTask.NAME)
        project.tasks.register<NpmSetupTask>(NpmSetupTask.NAME)
        project.tasks.register<YarnSetupTask>(YarnSetupTask.NAME)
    }

    private fun addNpmRule() { // note this rule also makes it possible to specify e.g. "dependsOn npm_install"
        project.tasks.addRule("Pattern: \"npm_<command>\": Executes an NPM command.") {
            val taskName = this
            if (taskName.startsWith("npm_")) {
                project.tasks.create<NpmTask>(taskName) {
                    val tokens = taskName.split("_").drop(1) // all except first
                    npmCommand.set(tokens)
                    if (tokens.first().equals("run", ignoreCase = true)) {
                        dependsOn(NpmInstallTask.NAME)
                    }
                }
            }
        }
    }

    private fun addYarnRule() { // note this rule also makes it possible to specify e.g. "dependsOn yarn_install"
        project.tasks.addRule("Pattern: \"yarn_<command>\": Executes an Yarn command.") {
            val taskName = this
            if (taskName.startsWith("yarn_")) {
                project.tasks.create<YarnTask>(taskName) {
                    val tokens = taskName.split("_").drop(1) // all except first
                    yarnCommand.set(tokens)
                    if (tokens.first().equals("run", ignoreCase = true)) {
                        dependsOn(YarnInstallTask.NAME)
                    }
                }
            }
        }
    }

    private fun addRepository(distUrl: String, allowInsecureProtocol: Boolean?) {
        project.repositories.ivy {
            name = "Node.js"
            setUrl(distUrl)
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("org.nodejs", "node")
            }
            if (GradleVersion.current() >= GradleVersion.version("6.0")) {
                allowInsecureProtocol?.let { isAllowInsecureProtocol = it }
            }
        }
    }

    private fun configureNodeSetupTask(nodeExtension: NodeExtension) {
        project.tasks.named<NodeSetupTask>(NodeSetupTask.NAME) {
            val nodeArchiveDependencyProvider = variantComputer.computeNodeArchiveDependency(nodeExtension)
            val archiveFileProvider = nodeArchiveDependencyProvider
                    .map { nodeArchiveDependency ->
                        resolveNodeArchiveFile(nodeArchiveDependency)
                    }
            nodeArchiveFile.set(project.layout.file(archiveFileProvider))
        }
    }

    private fun resolveNodeArchiveFile(name: String): File {
        val dependency = project.dependencies.create(name)
        val configuration = project.configurations.detachedConfiguration(dependency)
        configuration.isTransitive = false
        return configuration.resolve().single()
    }

    companion object {
        const val NODE_GROUP = "Node"
        const val NPM_GROUP = "npm"
        const val YARN_GROUP = "Yarn"
    }
}
