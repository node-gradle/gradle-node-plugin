package com.github.gradle.node.variant

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.util.mapIf
import com.github.gradle.node.util.tokenize
import com.github.gradle.node.util.zip
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

internal class VariantComputer @JvmOverloads constructor(
        private val platformHelper: PlatformHelper = PlatformHelper.INSTANCE
) {
    fun computeNodeDir(nodeExtension: NodeExtension): Provider<Directory> {
        return zip(nodeExtension.workDir, nodeExtension.version).map { (workDir, version) ->
            val osName = platformHelper.osName
            val osArch = platformHelper.osArch
            val dirName = "node-v$version-$osName-$osArch"
            workDir.dir(dirName)
        }
    }

    fun computeNodeBinDir(nodeDirProvider: Provider<Directory>) = computeProductBinDir(nodeDirProvider)

    fun computeNodeExec(nodeExtension: NodeExtension, nodeBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.download, nodeBinDirProvider).map {
            val (download, nodeBinDir) = it
            if (download) {
                val nodeCommand = if (platformHelper.isWindows) "node.exe" else "node"
                nodeBinDir.dir(nodeCommand).asFile.absolutePath
            } else "node"
        }
    }

    fun computeNpmDir(nodeExtension: NodeExtension, nodeDirProvider: Provider<Directory>): Provider<Directory> {
        return zip(nodeExtension.npmVersion, nodeExtension.npmWorkDir, nodeDirProvider).map {
            val (npmVersion, npmWorkDir, nodeDir) = it
            if (npmVersion.isNotBlank()) {
                val directoryName = "npm-v${npmVersion}"
                npmWorkDir.dir(directoryName)
            } else nodeDir
        }
    }

    fun computeNpmBinDir(npmDirProvider: Provider<Directory>) = computeProductBinDir(npmDirProvider)

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

    fun computeNpxExec(nodeExtension: NodeExtension, npmBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.download, nodeExtension.npxCommand, npmBinDirProvider).map {
            val (download, npxCommand, npmBinDir) = it
            val command = if (platformHelper.isWindows) {
                npxCommand.mapIf({ it == "npx" }) { "npx.cmd" }
            } else npxCommand
            if (download) npmBinDir.dir(command).asFile.absolutePath else command
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

    fun computeDependency(nodeExtension: NodeExtension): Provider<Dependency> {
        val osName = platformHelper.osName
        val osArch = platformHelper.osArch
        return if (platformHelper.isWindows) {
            hasWindowsZip(nodeExtension).flatMap { windowsZip ->
                if (windowsZip) {
                    computeArchiveDependency(nodeExtension, osName, osArch, "zip")
                            .map { archiveDependency -> Dependency(archiveDependency) }
                } else {
                    val archiveDependencyProvider =
                            computeArchiveDependency(nodeExtension, "linux", "x86", "tar.gz")
                    val exeDependencyProvider = computeExeDependency(nodeExtension)
                    zip(archiveDependencyProvider, exeDependencyProvider)
                            .map { (archiveDependency, exeDependency) ->
                                Dependency(archiveDependency, exeDependency)
                            }
                }
            }
        } else {
            computeArchiveDependency(nodeExtension, osName, osArch, "tar.gz")
                    .map { archiveDependency -> Dependency(archiveDependency) }
        }
    }

    internal data class Dependency(
            val archiveDependency: String,
            val exeDependency: String? = null
    )

    private fun hasWindowsZip(nodeExtension: NodeExtension): Provider<Boolean> {
        return nodeExtension.version.map { version ->
            val (majorVersion, minorVersion, microVersion) =
                    version.tokenize(".").map { it.toInt() }
            majorVersion == 4 && minorVersion >= 5
                    || majorVersion == 6 && (minorVersion > 2 || minorVersion == 2 && microVersion >= 1)
                    || majorVersion > 6
        }
    }

    private fun computeExeDependency(nodeExtension: NodeExtension): Provider<String> {
        return nodeExtension.version.map { version ->
            val majorVersion = version.tokenize(".").first().toInt()
            if (majorVersion > 3) {
                if (platformHelper.osArch == "x86") {
                    "org.nodejs:win-x86/node:${version}@exe"
                } else {
                    "org.nodejs:win-x64/node:${version}@exe"
                }
            } else {
                if (platformHelper.osArch == "x86") {
                    "org.nodejs:node:${version}@exe"
                } else {
                    "org.nodejs:x64/node:${version}@exe"
                }
            }
        }
    }

    private fun computeArchiveDependency(nodeExtension: NodeExtension, osName: String, osArch: String,
                                         type: String): Provider<String> {
        return nodeExtension.version.map { version -> "org.nodejs:node:$version:$osName-$osArch@$type" }
    }
}
