package com.github.gradle.node.variant

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.util.mapIf
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

    fun computePnpxExec(nodeExtension: NodeExtension, pnpmBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.download, nodeExtension.pnpxCommand, pnpmBinDirProvider).map {
            val (download, pnpxCommand, pnpmBinDir) = it
            val command = if (platformHelper.isWindows) {
                pnpxCommand.mapIf({ it == "pnpx" }) { "pnpx.cmd" }
            } else pnpxCommand
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

    fun computeNodeArchiveDependency(nodeExtension: NodeExtension): Provider<String> {
        val osName = platformHelper.osName
        val osArch = platformHelper.osArch
        val type = if (platformHelper.isWindows) "zip" else "tar.gz"
        return nodeExtension.version.map { version -> "org.nodejs:node:$version:$osName-$osArch@$type" }
    }
}
