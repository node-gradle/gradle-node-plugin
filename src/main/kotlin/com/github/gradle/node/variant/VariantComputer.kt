package com.github.gradle.node.variant

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.util.mapIf
import com.github.gradle.node.util.zip
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

open class VariantComputer constructor(
        private val platformHelper: PlatformHelper
) {
    constructor() : this(PlatformHelper.INSTANCE)

    /**
     * Get the expected directory for a given node version.
     *
     * Essentially: workingDir/node-v$version-$osName-$osArch
     */
    fun computeNodeDir(nodeExtension: NodeExtension): Provider<Directory> {
        return computeNodeDir(zip(nodeExtension.workDir, nodeExtension.version))
    }

    internal fun computeNodeDir(zip: Provider<Pair<Directory, String>>): Provider<Directory> {
        return zip.map { (workDir, version) ->
            val osName = platformHelper.osName
            val osArch = platformHelper.osArch
            val dirName = "node-v$version-$osName-$osArch"
            workDir.dir(dirName)
        }
    }

    /**
     * Get the expected node binary directory, taking Windows specifics into account.
     */
    fun computeNodeBinDir(nodeDirProvider: Provider<Directory>) = computeProductBinDir(nodeDirProvider)

    /**
     * Get the expected node binary name, node.exe on Windows and node everywhere else.
     */
    fun computeNodeExec(nodeExtension: NodeExtension, nodeBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.download, nodeBinDirProvider).map {
            val (download, nodeBinDir) = it
            if (download) {
                val nodeCommand = if (platformHelper.isWindows) "node.exe" else "node"
                nodeBinDir.dir(nodeCommand).asFile.absolutePath
            } else "node"
        }
    }

    /**
     * Get the expected directory for a given npm version.
     */
    fun computeNpmDir(nodeExtension: NodeExtension, nodeDirProvider: Provider<Directory>): Provider<Directory> {
        return zip(nodeExtension.npmVersion, nodeExtension.npmWorkDir, nodeDirProvider).map {
            val (npmVersion, npmWorkDir, nodeDir) = it
            if (npmVersion.isNotBlank()) {
                val directoryName = "npm-v${npmVersion}"
                npmWorkDir.dir(directoryName)
            } else nodeDir
        }
    }

    /**
     * Get the expected npm binary directory, taking Windows specifics into account.
     */
    fun computeNpmBinDir(npmDirProvider: Provider<Directory>) = computeProductBinDir(npmDirProvider)

    /**
     * Get the expected node binary name, npm.cmd on Windows and npm everywhere else.
     *
     * Can be overridden by setting npmCommand.
     */
    fun computeNpmExec(nodeExtension: NodeExtension, npmBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.download, nodeExtension.npmCommand, npmBinDirProvider).map {
            val (download, npmCommand, npmBinDir) = it
            val command = if (platformHelper.isWindows) {
                npmCommand.mapIf({ it == "npm" }) { "npm.cmd" }
            } else npmCommand
            if (download) npmBinDir.dir(command).asFile.absolutePath else command
        }
    }

    fun computeNpmScriptFile(nodeDirProvider: Provider<Directory>, command: String): Provider<String> {
        return nodeDirProvider.map { nodeDir ->
            if (platformHelper.isWindows) nodeDir.dir("node_modules/npm/bin/$command-cli.js").asFile.path
            else nodeDir.dir("lib/node_modules/npm/bin/$command-cli.js").asFile.path
        }
    }

    /**
     * Get the expected node binary name, npx.cmd on Windows and npx everywhere else.
     *
     * Can be overridden by setting npxCommand.
     */
    fun computeNpxExec(nodeExtension: NodeExtension, npmBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.download, nodeExtension.npxCommand, npmBinDirProvider).map {
            val (download, npxCommand, npmBinDir) = it
            val command = if (platformHelper.isWindows) {
                npxCommand.mapIf({ it == "npx" }) { "npx.cmd" }
            } else npxCommand
            if (download) npmBinDir.dir(command).asFile.absolutePath else command
        }
    }

    fun computePnpmDir(nodeExtension: NodeExtension): Provider<Directory> {
        return zip(nodeExtension.pnpmVersion, nodeExtension.pnpmWorkDir).map {
            val (pnpmVersion, pnpmWorkDir) = it
            val dirnameSuffix = if (pnpmVersion.isNotBlank()) {
                "-v${pnpmVersion}"
            } else "-latest"
            val dirname = "pnpm$dirnameSuffix"
            pnpmWorkDir.dir(dirname)
        }
    }

    fun computePnpmBinDir(pnpmDirProvider: Provider<Directory>) = computeProductBinDir(pnpmDirProvider)

    fun computePnpmExec(nodeExtension: NodeExtension, pnpmBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.pnpmCommand, nodeExtension.download, pnpmBinDirProvider).map {
            val (pnpmCommand, download, pnpmBinDir) = it
            val command = if (platformHelper.isWindows) {
                pnpmCommand.mapIf({ it == "pnpm" }) { "pnpm.cmd" }
            } else pnpmCommand
            if (download) pnpmBinDir.dir(command).asFile.absolutePath else command
        }
    }

    fun computeYarnDir(nodeExtension: NodeExtension): Provider<Directory> {
        return zip(nodeExtension.yarnVersion, nodeExtension.yarnWorkDir).map {
            val (yarnVersion, yarnWorkDir) = it
            val dirnameSuffix = if (yarnVersion.isNotBlank()) {
                "-v${yarnVersion}"
            } else "-latest"
            val dirname = "yarn$dirnameSuffix"
            yarnWorkDir.dir(dirname)
        }
    }

    fun computeYarnBinDir(yarnDirProvider: Provider<Directory>) = computeProductBinDir(yarnDirProvider)

    fun computeYarnExec(nodeExtension: NodeExtension, yarnBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.yarnCommand, nodeExtension.download, yarnBinDirProvider).map {
            val (yarnCommand, download, yarnBinDir) = it
            val command = if (platformHelper.isWindows) {
                yarnCommand.mapIf({ it == "yarn" }) { "yarn.cmd" }
            } else yarnCommand
            if (download) yarnBinDir.dir(command).asFile.absolutePath else command
        }
    }

    private fun computeProductBinDir(productDirProvider: Provider<Directory>) =
            if (platformHelper.isWindows) productDirProvider else productDirProvider.map { it.dir("bin") }

    /**
     * Get the node archive name in Gradle dependency format, using zip for Windows and tar.gz everywhere else.
     *
     * Essentially: org.nodejs:node:$version:$osName-$osArch@tar.gz
     */
    fun computeNodeArchiveDependency(nodeExtension: NodeExtension): Provider<String> {
        val osName = platformHelper.osName
        val osArch = platformHelper.osArch
        val type = if (platformHelper.isWindows) "zip" else "tar.gz"
        return nodeExtension.version.map { version -> "org.nodejs:node:$version:$osName-$osArch@$type" }
    }
}
