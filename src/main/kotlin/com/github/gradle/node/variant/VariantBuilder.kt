package com.github.gradle.node.variant

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.util.mapIf
import com.github.gradle.node.util.tokenize
import java.io.File

class VariantBuilder @JvmOverloads constructor(
        private val platformHelper: PlatformHelper = PlatformHelper.INSTANCE
) {
    fun build(nodeExtension: NodeExtension): Variant {
        val osName = platformHelper.osName
        val osArch = platformHelper.osArch
        val isWindows = platformHelper.isWindows

        var nodeExec = "node"
        val nodeDir: File = computeNodeDir(nodeExtension, osName, osArch)
        val nodeBinDir: File
        var npmExec: String
        val npmDir: File = if (nodeExtension.npmVersion.isNotBlank()) computeNpmDir(nodeExtension) else nodeDir
        val npmBinDir: File
        val npmScriptFile: String
        var npxExec: String
        val npxScriptFile: String
        var yarnExec: String
        val yarnDir: File = computeYarnDir(nodeExtension)
        val yarnBinDir: File
        val archiveDependency: String
        val exeDependency: String?

        if (isWindows) {
            nodeBinDir = nodeDir
            npmExec = nodeExtension.npmCommand.mapIf({ it == "npm" }) { "npm.cmd" }
            npmBinDir = npmDir
            npmScriptFile = File(nodeDir, "node_modules/npm/bin/npm-cli.js").path
            npxExec = nodeExtension.npxCommand.mapIf({ it == "npx" }) { "npx.cmd" }
            npxScriptFile = File(nodeDir, "node_modules/npm/bin/npx-cli.js").path
            yarnExec = nodeExtension.yarnCommand.mapIf({ it == "yarn" }) { "yarn.cmd" }
            yarnBinDir = yarnDir
            if (hasWindowsZip(nodeExtension)) {
                archiveDependency = computeArchiveDependency(nodeExtension, osName, osArch, "zip")
                exeDependency = null
            } else {
                archiveDependency =
                        computeArchiveDependency(nodeExtension, "linux", "x86", "tar.gz")
                exeDependency = computeExeDependency(nodeExtension)
            }
        } else {
            nodeBinDir = File(nodeDir, "bin")
            npmExec = nodeExtension.npmCommand
            npmBinDir = File(npmDir, "bin")
            npmScriptFile = File(nodeDir, "lib/node_modules/npm/bin/npm-cli.js").path
            npxExec = nodeExtension.npxCommand
            npxScriptFile = File(nodeDir, "lib/node_modules/npm/bin/npx-cli.js").path
            yarnExec = nodeExtension.yarnCommand
            yarnBinDir = File(yarnDir, "bin")
            archiveDependency = computeArchiveDependency(nodeExtension, osName, osArch, "tar.gz")
            exeDependency = null
        }

        if (nodeExtension.download) {
            nodeExec = nodeExec.mapIf({ it == "node" && isWindows }) { "node.exe" }
            nodeExec = File(nodeBinDir, nodeExec).absolutePath
            npmExec = File(npmBinDir, npmExec).absolutePath
            npxExec = File(npmBinDir, npxExec).absolutePath
            yarnExec = File(yarnBinDir, yarnExec).absolutePath
        }

        return Variant(isWindows, nodeExec, nodeDir, nodeBinDir, npmExec, npmDir, npmBinDir, npmScriptFile,
                npxExec, npxScriptFile, yarnExec, yarnDir, yarnBinDir, archiveDependency, exeDependency
        )
    }

    private fun computeArchiveDependency(nodeExtension: NodeExtension, osName: String?, osArch: String?,
                                         type: String): String {
        val version = nodeExtension.version
        return "org.nodejs:node:$version:$osName-$osArch@$type"
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

    private fun hasWindowsZip(nodeExtension: NodeExtension): Boolean {
        val (majorVersion, minorVersion, microVersion) = nodeExtension.version.tokenize(".").map { it.toInt() }
        return majorVersion == 4 && minorVersion >= 5
                || majorVersion == 6 && (minorVersion > 2 || minorVersion == 2 && microVersion >= 1)
                || majorVersion > 6
    }

    private fun computeNpmDir(nodeExtension: NodeExtension): File {
        return File(nodeExtension.npmWorkDir, "npm-v${nodeExtension.npmVersion}")
    }

    private fun computeYarnDir(nodeExtension: NodeExtension): File {
        val dirnameSuffix = if (nodeExtension.yarnVersion.isNotBlank()) "-v${nodeExtension.yarnVersion}" else "-latest"
        val dirname = "yarn$dirnameSuffix"
        return File(nodeExtension.yarnWorkDir, dirname)
    }

    private fun computeNodeDir(nodeExtension: NodeExtension, osName: String?, osArch: String?): File {
        val version = nodeExtension.version
        val dirName = "node-v$version-$osName-$osArch"
        return File(nodeExtension.workDir, dirName)
    }
}
