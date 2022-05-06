package com.github.gradle.node

import com.github.gradle.node.npm.proxy.ProxySettings
import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmSetupTask
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.npm.task.NpxTask
import com.github.gradle.node.pnpm.task.PnpmInstallTask
import com.github.gradle.node.pnpm.task.PnpmSetupTask
import com.github.gradle.node.pnpm.task.PnpmTask
import com.github.gradle.node.services.NodePathTestTask
import com.github.gradle.node.services.NodeRuntime
import com.github.gradle.node.services.NodeToolchainServiceImpl
import com.github.gradle.node.services.NpmPathTestTask
import com.github.gradle.node.services.api.NodeToolchainService
import com.github.gradle.node.task.NodeSetupTask
import com.github.gradle.node.task.NodeTask
import com.github.gradle.node.yarn.task.YarnInstallTask
import com.github.gradle.node.yarn.task.YarnSetupTask
import com.github.gradle.node.yarn.task.YarnTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*
import org.gradle.util.GradleVersion
import java.io.File

@Suppress("UnstableApiUsage")
class NodePlugin : Plugin<Project> {
    private lateinit var project: Project

    private lateinit var runtime: Provider<NodeRuntime>

    private lateinit var experimentalEnabled: Property<Boolean>

    override fun apply(project: Project) {
        if (GradleVersion.current() < GradleVersion.version("6.1")) {
            throw RuntimeException("This plugin version requires Gradle 6.1 or newer.")
        }

        this.project = project
        val nodeExtension = NodeExtension.create(project)
        project.extensions.create<PackageJsonExtension>(PackageJsonExtension.NAME, project)
        experimentalEnabled = project.objects.property<Boolean>().convention(false)
        if (GradleVersion.current() >= GradleVersion.version("6.2")) {
            experimentalEnabled.set(
            project.providers.gradleProperty(EXPERIMENTAL_PROP)
                .forUseAtConfigurationTime()
                .getOrElse("false").toBoolean())
        }
        if (experimentalEnabled.get()) {
            runtime = project.gradle.sharedServices.registerIfAbsent("nodeRuntime", NodeRuntime::class) {
                parameters.gradleUserHome.set(project.gradle.gradleUserHomeDir)
            }

            val service = NodeToolchainServiceImpl(runtime, project.providers, nodeExtension)
            project.extensions.add(NodeToolchainService::class, "nodeToolchainService", service)

            project.tasks.register<NodePathTestTask>("nodePathTest").configure {
                usesService(runtime)
                nodeRuntime.set(runtime)
            }

            project.tasks.register<NpmPathTestTask>("npmPathTest").configure {
                usesService(runtime)
                nodeRuntime.set(runtime)
            }

        }
        addGlobalTypes()
        addTasks()
        addNpmRule()
        addPnpmRule()
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
        addGlobalType<PnpmTask>()
        addGlobalType<YarnTask>()
        addGlobalType<ProxySettings>()
    }

    private inline fun <reified T> addGlobalType() {
        project.extensions.extraProperties[T::class.java.simpleName] = T::class.java
    }

    private fun addTasks() {
        val npmInstall  = project.tasks.register<NpmInstallTask>(NpmInstallTask.NAME)
        val pnpmInstall = project.tasks.register<PnpmInstallTask>(PnpmInstallTask.NAME)
        val yarnInstall = project.tasks.register<YarnInstallTask>(YarnInstallTask.NAME)
        val nodeSetup   = project.tasks.register<NodeSetupTask>(NodeSetupTask.NAME)
        val npmSetup    = project.tasks.register<NpmSetupTask>(NpmSetupTask.NAME)
        val pnpmSetup   = project.tasks.register<PnpmSetupTask>(PnpmSetupTask.NAME)
        val yarnSetup   = project.tasks.register<YarnSetupTask>(YarnSetupTask.NAME)

        if (experimentalEnabled.get()) {
            nodeSetup.configure { onlyIf { false } }
            npmSetup.configure {
                if (experimentalEnabled.get()) {
                    usesService(runtime)
                    nodeRuntime.set(runtime)
                    experimental.set(experimentalEnabled)
                }
            }
        }
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

    private fun addPnpmRule() { // note this rule also makes it possible to specify e.g. "dependsOn npm_install"
        project.tasks.addRule("Pattern: \"pnpm_<command>\": Executes an PNPM command.") {
            val taskName = this
            if (taskName.startsWith("pnpm_")) {
                project.tasks.register<PnpmTask>(taskName) {
                    val tokens = taskName.split("_").drop(1) // all except first
                    pnpmCommand.set(tokens)
                    if (tokens.first().equals("run", ignoreCase = true)) {
                        dependsOn(PnpmInstallTask.NAME)
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
        const val PNPM_GROUP = "pnpm"
        const val YARN_GROUP = "Yarn"
        const val EXPERIMENTAL_PROP = "com.github.gradle.node.experimental"
        const val DETECT_PROP = "com.github.gradle.node.installations.location"
        const val DOWNLOAD_PROP = "com.github.gradle.node.installations.auto-download"
        const val URL_PROP = "com.github.gradle.node.installations.baseUrl"
        const val URL_DEFAULT = "https://nodejs.org/dist"
    }
}
