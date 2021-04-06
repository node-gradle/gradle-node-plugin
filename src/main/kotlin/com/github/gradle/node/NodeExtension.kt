package com.github.gradle.node

import com.github.gradle.node.npm.proxy.ProxySettings
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property

open class NodeExtension(project: Project) {
    private val cacheDir = project.layout.projectDirectory.dir(".gradle")

    /**
     * The directory where Node.js is unpacked (when download is true)
     */
    val workDir = project.objects.directoryProperty().convention(cacheDir.dir("nodejs"))

    /**
     * The directory where npm is installed (when a specific version is defined)
     */
    val npmWorkDir = project.objects.directoryProperty().convention(cacheDir.dir("npm"))

    /**
     * The directory where yarn is installed (when a Yarn task is used)
     */
    val yarnWorkDir = project.objects.directoryProperty().convention(cacheDir.dir("yarn"))

    /**
     * The directory where pnpm is installed (when a Yarn task is used)
     */
    val pnpmWorkDir = project.objects.directoryProperty().convention(cacheDir.dir("pnpm"))

    /**
     * The Node.js project directory location
     * This is where the package.json file and node_modules directory are located
     * By default it is at the root of the current project
     */
    val nodeProjectDir = project.objects.directoryProperty().convention(project.layout.projectDirectory)

    /**
     * Version of node to download and install (only used if download is true)
     * It will be unpacked in the workDir
     */
    val version = project.objects.property<String>().convention(DEFAULT_NODE_VERSION)

    /**
     * Version of npm to use
     * If specified, installs it in the npmWorkDir
     * If empty, the plugin will use the npm command bundled with Node.js
     */
    val npmVersion = project.objects.property<String>().convention("")

    /**
     * Version of Yarn to use
     * Any Yarn task first installs Yarn in the yarnWorkDir
     * It uses the specified version if defined and the latest version otherwise (by default)
     */
    val yarnVersion = project.objects.property<String>().convention("")

    /**
     * Version of pnpm to use
     * Any pnpm task first installs pnpm in the pnpmWorkDir
     * It uses the specified version if defined and the latest version otherwise (by default)
     */
    val pnpmVersion = project.objects.property<String>().convention("")

    /**
     * Base URL for fetching node distributions
     * Only used if download is true
     * Change it if you want to use a mirror
     * Or set to null if you want to add the repository on your own.
     */
    val distBaseUrl = project.objects.property<String>()

    /**
     * The command used to start npm (default npm)
     */
    val npmCommand = project.objects.property<String>().convention("npm")

    /**
     * The command used to start npx (default npx)
     */
    val npxCommand = project.objects.property<String>().convention("npx")

    /**
     * The command used to start yarn (default yarn)
     */
    val yarnCommand = project.objects.property<String>().convention("yarn")

    /**
     * The command used to start pnpm (default pnpm)
     */
    val pnpmCommand = project.objects.property<String>().convention("pnpm")

    /**
     * The command used to start pnpx (default pnpx)
     */
    val pnpxCommand = project.objects.property<String>().convention("pnpx")

    /**
     * The npm command executed by the npmInstall task
     * By default it is install but it can be changed to ci
     */
    val npmInstallCommand = project.objects.property<String>().convention("install")

    /**
     * Whether to download and install a specific Node.js version or not
     * If false, it will use the globally installed Node.js
     * If true, it will download node using above parameters
     * Note that npm is bundled with Node.js
     */
    val download = project.objects.property<Boolean>().convention(false)

    /**
     * Whether the plugin automatically should add the proxy configuration to npm and yarn commands
     * according the proxy configuration defined for Gradle
     *
     * Disable this option if you want to configure the proxy for npm or yarn on your own
     * (in the .npmrc file for instance)
     *
     */
    val nodeProxySettings = project.objects.property<ProxySettings>().convention(ProxySettings.SMART)

    @Suppress("unused")
    @Deprecated("Deprecated in version 3.0, please use nodeProjectDir now")
    val nodeModulesDir = nodeProjectDir

    init {
        distBaseUrl.set("https://nodejs.org/dist")
    }

    @Deprecated("useGradleProxySettings has been replaced with nodeProxySettings",
        replaceWith = ReplaceWith("nodeProxySettings.set(i)"))
    fun setUseGradleProxySettings(value: Boolean) {
        nodeProxySettings.set(if (value) ProxySettings.SMART else ProxySettings.OFF)
    }

    companion object {
        const val NAME = "node"
        const val DEFAULT_NODE_VERSION = "14.15.4"
        const val DEFAULT_NPM_VERSION = "6.14.10"

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
