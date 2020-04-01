package com.github.gradle.node.variant

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.util.mapIf
import com.github.gradle.node.util.tokenize
import java.io.File

class VariantBuilder @JvmOverloads constructor(
        private val ext: NodeExtension,
        private val platformHelper: PlatformHelper = PlatformHelper.INSTANCE
) {

    @Suppress("CanBeVal")
    fun build(): Variant {
        val osName = platformHelper.osName
        val osArch = platformHelper.osArch
        val isWindows = platformHelper.isWindows

        var nodeExec: String
        var nodeDir: File
        var nodeBinDir: File
        var npmExec: String
        var npmDir: File
        var npmBinDir: File
        var npmScriptFile: String
        var npxExec: String
        var npxScriptFile: String
        var yarnExec: String
        var yarnDir: File
        var yarnBinDir: File
        var archiveDependency: String
        var exeDependency: String?

        if (isWindows) {
            nodeExec = "node"
            nodeDir = getNodeDir(osName, osArch)
            nodeBinDir = nodeDir
            npmExec = ext.npmCommand.mapIf({ it == "npm" }) { "npm.cmd" }
            npmDir = if (ext.npmVersion.isNotBlank()) getNpmDir() else nodeDir
            npmBinDir = npmDir
            npmScriptFile = File(nodeDir, "node_modules/npm/bin/npm-cli.js").path
            npxExec = ext.npxCommand.mapIf({ it == "npx" }) { "npx.cmd" }
            npxScriptFile = File(nodeDir, "node_modules/npm/bin/npx-cli.js").path
            yarnExec = ext.yarnCommand.mapIf({ it == "yarn" }) { "yarn.cmd" }
            yarnDir = getYarnDir()
            yarnBinDir = yarnDir
            if (hasWindowsZip()) {
                archiveDependency = getArchiveDependency(osName, osArch, "zip")
                exeDependency = null
            } else {
                archiveDependency = getArchiveDependency("linux", "x86", "tar.gz")
                exeDependency = getExeDependency()
            }
        } else {
            nodeExec = "node"
            nodeDir = getNodeDir(osName, osArch)
            nodeBinDir = File(nodeDir, "bin")
            npmExec = ext.npmCommand
            npmDir = if (ext.npmVersion.isNotBlank()) getNpmDir() else nodeDir
            npmBinDir = File(npmDir, "bin")
            npmScriptFile = File(nodeDir, "lib/node_modules/npm/bin/npm-cli.js").path
            npxExec = ext.npxCommand
            npxScriptFile = File(nodeDir, "lib/node_modules/npm/bin/npx-cli.js").path
            yarnExec = ext.yarnCommand
            yarnDir = getYarnDir()
            yarnBinDir = File(yarnDir, "bin")
            archiveDependency = getArchiveDependency(osName, osArch, "tar.gz")
            exeDependency = null
        }

        if (ext.download) {
            nodeExec = nodeExec.mapIf({ it == "node" && isWindows }) { "node.exe" }
            nodeExec = File(nodeBinDir, nodeExec).absolutePath
            npmExec = File(npmBinDir, npmExec).absolutePath
            npxExec = File(npmBinDir, npxExec).absolutePath
            yarnExec = File(yarnBinDir, yarnExec).absolutePath
        }

        return Variant(
                isWindows = isWindows,
                nodeExec = nodeExec,
                nodeDir = nodeDir,
                nodeBinDir = nodeBinDir,
                npmExec = npmExec,
                npmDir = npmDir,
                npmBinDir = npmBinDir,
                npmScriptFile = npmScriptFile,
                npxExec = npxExec,
                npxScriptFile = npxScriptFile,
                yarnExec = yarnExec,
                yarnDir = yarnDir,
                yarnBinDir = yarnBinDir,
                archiveDependency = archiveDependency,
                exeDependency = exeDependency
        )
    }

    private fun getArchiveDependency(osName: String?, osArch: String?, type: String): String {
        val version = ext.version
        return "org.nodejs:node:$version:$osName-$osArch@$type"
    }

    private fun getExeDependency(): String {
        val majorVersion = ext.version.tokenize(".").first().toInt()
        return if (majorVersion > 3) {
            if (platformHelper.osArch == "x86") {
                "org.nodejs:win-x86/node:${ext.version}@exe"
            } else {
                "org.nodejs:win-x64/node:${ext.version}@exe"
            }
        } else {
            if (platformHelper.osArch == "x86") {
                "org.nodejs:node:${ext.version}@exe"
            } else {
                "org.nodejs:x64/node:${ext.version}@exe"
            }
        }
    }

    private fun hasWindowsZip(): Boolean {
        val (majorVersion, minorVersion, microVersion) = ext.version.tokenize(".").map { it.toInt() }
        return majorVersion == 4 && minorVersion >= 5
                || majorVersion == 6 && (minorVersion > 2 || minorVersion == 2 && microVersion >= 1)
                || majorVersion > 6
    }

    private fun getNpmDir(): File {
        return File(ext.npmWorkDir, "npm-v${ext.npmVersion}")
    }

    private fun getYarnDir(): File {
        val dirname = "yarn" + if (ext.yarnVersion.isNotBlank()) "-v${ext.yarnVersion}" else "-latest"
        return File(ext.yarnWorkDir, dirname)
    }

    private fun getNodeDir(osName: String?, osArch: String?): File {
        val version = ext.version
        val dirName = "node-v$version-$osName-$osArch"
        return File(ext.workDir, dirName)
    }
}
