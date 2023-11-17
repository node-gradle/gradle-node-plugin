package com.github.gradle.node

import com.github.gradle.node.npm.proxy.ProxySettings
import com.github.gradle.node.util.Platform
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property

abstract class NodeExtension(project: Project) : ExtensionAware {
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
     * The directory where Bun is installed (when a Bun task is used)
     */
    val bunWorkDir = project.objects.directoryProperty().convention(cacheDir.dir("bun"))

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
     * Version of Bun to use
     * Any Bun task first installs Bun in the bunWorkDir
     * It uses the specified version if defined and the latest version otherwise (by default)
     */
    val bunVersion = project.objects.property<String>().convention("")

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
    val bunCommand = project.objects.property<String>().convention("bun")
    val bunxCommand = project.objects.property<String>().convention("bunx")

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

    /**
     * Disable functionality that requires newer versions of npm
     *
     * If you're not downloading Node.js and using old version of Node or npm
     * set this to true to disable functionality that makes use of newer functionality.
     *
     * This will be removed in 4.x
     */
    val oldNpm = project.objects.property<Boolean>().convention(false)

    /**
     * Create rules for automatic task creation
     *
     * Disabling this will prevent the npm_ npx_ yarn_ pnpm_ tasks from being
     * automatically created.
     * It's recommended to turn this off after you've gotten comfortable
     * with the plugin and register your own tasks instead of relying on the rule.
     */
    val enableTaskRules = project.objects.property<Boolean>().convention(true)


    /**
     * Computed path to nodejs directory
     */
    val resolvedNodeDir = project.objects.directoryProperty()

    /**
     * Operating system and architecture
     */
    val resolvedPlatform = project.objects.property<Platform>()

    init {
        distBaseUrl.set("https://nodejs.org/dist")
    }

    companion object {
        /**
         * Extension name in Gradle
         */
        const val NAME = "node"

        /**
         * Default version of Node to download if none is set
         */
        const val DEFAULT_NODE_VERSION = "18.17.1"

        /**
         * Default version of npm to download if none is set
         */
        const val DEFAULT_NPM_VERSION = "9.6.7"

        @JvmStatic
        operator fun get(project: Project): NodeExtension {
            return project.extensions.getByType()
        }

        @JvmStatic
        fun create(project: Project): NodeExtension {
            return project.extensions.create<NodeExtension>(NAME, project, )
        }
    }
}
