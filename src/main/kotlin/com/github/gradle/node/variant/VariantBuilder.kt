package com.github.gradle.node.variant

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.util.mapIf
import com.github.gradle.node.util.tokenize
import java.io.File

internal class VariantBuilder @JvmOverloads constructor(
        private val platformHelper: PlatformHelper = PlatformHelper.INSTANCE
) {
    fun build(nodeExtension: NodeExtension): Variant {
        val nodeDir = computeNodeDir(nodeExtension)
        val nodeBinDir = computeNodeBinDir(nodeDir)
        val nodeExec = computeNodeExec(nodeExtension, nodeBinDir)
        val npmDir = computeNpmDir(nodeExtension, nodeDir)
        val npmBinDir = computeNodeBinDir(npmDir)
        val npmExec = computeNpmExec(nodeExtension, npmBinDir)
        val npmScriptFile = computeNpmScriptFile(nodeDir, "npm")
        val npxExec = computeNpxExec(nodeExtension, npmBinDir)
        val npxScriptFile = computeNpmScriptFile(nodeDir, "npx")
        val yarnDir = computeYarnDir(nodeExtension)
        val yarnBinDir = computeNodeBinDir(yarnDir)
        val yarnExec = computeYarnExec(nodeExtension, yarnBinDir)
        val dependency = computeDependency(nodeExtension)

        return Variant(platformHelper.isWindows, nodeExec, nodeDir, nodeBinDir, npmExec, npmDir, npmBinDir,
                npmScriptFile, npxExec, npxScriptFile, yarnExec, yarnDir, yarnBinDir, dependency.archiveDependency,
                dependency.exeDependency)
    }

    private fun computeNodeDir(nodeExtension: NodeExtension): File {
        val osName = platformHelper.osName
        val osArch = platformHelper.osArch
        val version = nodeExtension.version
        val dirName = "node-v$version-$osName-$osArch"
        return File(nodeExtension.workDir, dirName)
    }

    private fun computeNodeBinDir(nodeDir: File) =
            if (platformHelper.isWindows) nodeDir else File(nodeDir, "bin")

    private fun computeNodeExec(nodeExtension: NodeExtension, nodeBinDir: File): String {
        if (nodeExtension.download) {
            val nodeCommand = if (platformHelper.isWindows) "node.exe" else "node"
            return File(nodeBinDir, nodeCommand).absolutePath
        }
        return "node"
    }

    private fun computeNpmDir(nodeExtension: NodeExtension, nodeDir: File): File {
        return if (nodeExtension.npmVersion.isNotBlank()) {
            val directoryName = "npm-v${nodeExtension.npmVersion}"
            File(nodeExtension.npmWorkDir, directoryName)
        } else nodeDir
    }

    private fun computeNpmExec(nodeExtension: NodeExtension, npmBinDir: File): String {
        val npmCommand = if (platformHelper.isWindows) {
            nodeExtension.npmCommand.mapIf({ it == "npm" }) { "npm.cmd" }
        } else nodeExtension.npmCommand
        return if (nodeExtension.download) File(npmBinDir, npmCommand).absolutePath else npmCommand
    }

    private fun computeNpmScriptFile(nodeDir: File, command: String): String {
        return if (platformHelper.isWindows) File(nodeDir, "node_modules/npm/bin/$command-cli.js").path
        else File(nodeDir, "lib/node_modules/npm/bin/$command-cli.js").path
    }

    private fun computeNpxExec(nodeExtension: NodeExtension, npmBinDir: File): String {
        val npxCommand = if (platformHelper.isWindows) {
            nodeExtension.npxCommand.mapIf({ it == "npx" }) { "npx.cmd" }
        } else nodeExtension.npxCommand
        return if (nodeExtension.download) File(npmBinDir, npxCommand).absolutePath else npxCommand
    }

    private fun computeYarnDir(nodeExtension: NodeExtension): File {
        val dirnameSuffix = if (nodeExtension.yarnVersion.isNotBlank()) {
            "-v${nodeExtension.yarnVersion}"
        } else "-latest"
        val dirname = "yarn$dirnameSuffix"
        return File(nodeExtension.yarnWorkDir, dirname)
    }

    private fun computeYarnExec(nodeExtension: NodeExtension, yarnBinDir: File): String {
        val yarnCommand = if (platformHelper.isWindows) {
            nodeExtension.yarnCommand.mapIf({ it == "yarn" }) { "yarn.cmd" }
        } else nodeExtension.yarnCommand
        return if (nodeExtension.download) File(yarnBinDir, yarnCommand).absolutePath else yarnCommand
    }

    private fun computeDependency(nodeExtension: NodeExtension): Dependency {
        val osName = platformHelper.osName
        val osArch = platformHelper.osArch
        return if (platformHelper.isWindows) {
            return if (hasWindowsZip(nodeExtension)) {
                val archiveDependency = computeArchiveDependency(nodeExtension, osName, osArch, "zip")
                Dependency(archiveDependency)
            } else {
                val archiveDependency =
                        computeArchiveDependency(nodeExtension, "linux", "x86", "tar.gz")
                val exeDependency = computeExeDependency(nodeExtension)
                Dependency(archiveDependency, exeDependency)
            }
        } else {
            val archiveDependency = computeArchiveDependency(nodeExtension, osName, osArch, "tar.gz")
            Dependency(archiveDependency)
        }
    }

    private data class Dependency(
            val archiveDependency: String,
            val exeDependency: String? = null
    )

    private fun hasWindowsZip(nodeExtension: NodeExtension): Boolean {
        val (majorVersion, minorVersion, microVersion) =
                nodeExtension.version.tokenize(".").map { it.toInt() }
        return majorVersion == 4 && minorVersion >= 5
                || majorVersion == 6 && (minorVersion > 2 || minorVersion == 2 && microVersion >= 1)
                || majorVersion > 6
    }

    private fun computeExeDependency(nodeExtension: NodeExtension): String {
        val majorVersion = nodeExtension.version.tokenize(".").first().toInt()
        return if (majorVersion > 3) {
            if (platformHelper.osArch == "x86") {
                "org.nodejs:win-x86/node:${nodeExtension.version}@exe"
            } else {
                "org.nodejs:win-x64/node:${nodeExtension.version}@exe"
            }
        } else {
            if (platformHelper.osArch == "x86") {
                "org.nodejs:node:${nodeExtension.version}@exe"
            } else {
                "org.nodejs:x64/node:${nodeExtension.version}@exe"
            }
        }
    }

    private fun computeArchiveDependency(nodeExtension: NodeExtension, osName: String?, osArch: String?,
                                         type: String): String {
        val version = nodeExtension.version
        return "org.nodejs:node:$version:$osName-$osArch@$type"
    }
}
