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
     * The directory where pnpm is installed (when a pnpm task is used)
     */
    val pnpmWorkDir = project.objects.directoryProperty().convention(cacheDir.dir("pnpm"))

    /**
     * The directory where yarn is installed (when a Yarn task is used)
     */
    val yarnWorkDir = project.objects.directoryProperty().convention(cacheDir.dir("yarn"))

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
     * Version of pnpm to use
     * Any pnpm task first installs pnpm in the pnpmWorkDir
     * It uses the specified version if defined and the latest version otherwise (by default)
     */
    val pnpmVersion = project.objects.property<String>().convention("")

    /**
     * Version of Yarn to use
     * Any Yarn task first installs Yarn in the yarnWorkDir
     * It uses the specified version if defined and the latest version otherwise (by default)
     */
    val yarnVersion = project.objects.property<String>().convention("")

    /**
     * Base URL for fetching node distributions
     * Only used if download is true
     * Change it if you want to use a mirror
     * Or set to null if you want to add the repository on your own.
     */
    val distBaseUrl = project.objects.property<String>()

    /**
     * Specifies whether it is acceptable to communicate with the Node.js repository over an insecure HTTP connection.
     * Only used if download is true
     * Change it to true if you use a mirror that uses HTTP rather than HTTPS
     * Or set to null if you want to use Gradle's default behaviour.
     */
    val allowInsecureProtocol = project.objects.property<Boolean>()

    val npmCommand = project.objects.property<String>().convention("npm")
    val npxCommand = project.objects.property<String>().convention("npx")
    val pnpmCommand = project.objects.property<String>().convention("pnpm")
    val yarnCommand = project.objects.property<String>().convention("yarn")

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

    /**
     * Use fast NpmInstall logic, excluding node_modules for output tracking resulting in a significantly faster
     * npm install/ci configuration at the cost of slightly decreased correctness in certain circumstances.
     *
     * In practice this means that if you change node_modules through other means than npm install/ci
     * NpmInstall tasks will continue being up-to-date, but if you're modifying node_modules through
     * other tools you may have other correctness problems and surfacing them here may be preferred.
     *
     * https://docs.npmjs.com/cli/v8/configuring-npm/package-lock-json#hidden-lockfiles
     *
     * Requires npm 7 or later
     * This will become the default in 4.x
     */
    val fastNpmInstall = project.objects.property<Boolean>().convention(false)

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
        /**
         * Extension name in Gradle
         */
        const val NAME = "node"

        /**
         * Default version of Node to download if none is set
         */
        const val DEFAULT_NODE_VERSION = "16.14.2"

        /**
         * Default version of npm to download if none is set
         */
        const val DEFAULT_NPM_VERSION = "8.5.0"

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
