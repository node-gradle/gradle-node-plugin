package com.github.gradle.node.variant

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.Platform
import com.github.gradle.node.util.mapIf
import com.github.gradle.node.util.zip
import org.gradle.api.file.Directory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * Get the expected node binary name, node.exe on Windows and node everywhere else.
 */
fun computeNodeExec(nodeExtension: NodeExtension, nodeBinDirProvider: Provider<Directory>): Provider<String> {
    return zip(nodeExtension.download, nodeBinDirProvider).map {
        val (download, nodeBinDir) = it
        if (download) {
            val nodeCommand = if (nodeExtension.resolvedPlatform.get().isWindows()) "node.exe" else "node"
            nodeBinDir.dir(nodeCommand).asFile.absolutePath
        } else "node"
    }
}
fun computeNpmScriptFile(nodeDirProvider: Provider<Directory>, command: String, isWindows: Boolean): Provider<String> {
    return nodeDirProvider.map { nodeDir ->
        if (isWindows) nodeDir.dir("node_modules/npm/bin/$command-cli.js").asFile.path
        else nodeDir.dir("lib/node_modules/npm/bin/$command-cli.js").asFile.path
    }
}

fun computeNodeDir(nodeExtension: NodeExtension): Provider<Directory> {
    val osName = nodeExtension.resolvedPlatform.get().name
    val osArch = nodeExtension.resolvedPlatform.get().arch
    return computeNodeDir(nodeExtension, osName, osArch)
}

fun computeNodeDir(nodeExtension: NodeExtension, osName: String, osArch: String): Provider<Directory> {
    return zip(nodeExtension.workDir, nodeExtension.version).map { (workDir, version) ->
        val dirName = "node-v$version-$osName-$osArch"
        workDir.dir(dirName)
    }
}

/**
 * Get the node archive name in Gradle dependency format, using zip for Windows and tar.gz everywhere else.
 *
 * Essentially: org.nodejs:node:$version:$osName-$osArch@tar.gz
 */
fun computeNodeArchiveDependency(extension: NodeExtension): Provider<String> {
    val osName = extension.resolvedPlatform.get().name
    val osArch = extension.resolvedPlatform.get().arch
    val type = if (extension.resolvedPlatform.get().isWindows()) "zip" else "tar.gz"
    return extension.version.map { version -> "org.nodejs:node:$version:$osName-$osArch@$type" }
}

open class VariantComputer {
    /**
     * Get the expected node binary directory, taking Windows specifics into account.
     */
    fun computeNodeBinDir(nodeDirProvider: Provider<Directory>, platform: Property<Platform>) = computeProductBinDir(nodeDirProvider, platform)

    /**
     * Get the expected node binary name, node.exe on Windows and node everywhere else.
     */
    @Deprecated(message = "replaced by package-level function",
        replaceWith =
        ReplaceWith("com.github.gradle.node.variant.computeNodeExec(nodeExtension, nodeBinDirProvider)"))
    fun computeNodeExec(nodeExtension: NodeExtension, nodeBinDirProvider: Provider<Directory>): Provider<String> {
        return com.github.gradle.node.variant.computeNodeExec(nodeExtension, nodeBinDirProvider)
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
    fun computeNpmBinDir(npmDirProvider: Provider<Directory>, platform: Property<Platform>) = computeProductBinDir(npmDirProvider, platform)

    /**
     * Get the expected node binary name, npm.cmd on Windows and npm everywhere else.
     *
     * Can be overridden by setting npmCommand.
     */
    fun computeNpmExec(nodeExtension: NodeExtension, npmBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.download, nodeExtension.npmCommand, npmBinDirProvider).map {
            val (download, npmCommand, npmBinDir) = it
            val command = if (nodeExtension.resolvedPlatform.get().isWindows()) {
                npmCommand.mapIf({ it == "npm" }) { "npm.cmd" }
            } else npmCommand
            if (download) npmBinDir.dir(command).asFile.absolutePath else command
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
            val command = if (nodeExtension.resolvedPlatform.get().isWindows()) {
                npxCommand.mapIf({ it == "npx" }) { "npx.cmd" }
            } else npxCommand
            if (download) npmBinDir.dir(command).asFile.absolutePath else command
        }
    }

    /**
     * Get the expected bunx binary name, bunx.cmd on Windows and bunx everywhere else.
     *
     * Can be overridden by setting bunxCommand.
     */
    fun computeBunxExec(nodeExtension: NodeExtension, bunBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.download, nodeExtension.bunxCommand, bunBinDirProvider).map {
            val (download, bunxCommand, bunBinDir) = it
            val command = if (nodeExtension.resolvedPlatform.get().isWindows()) {
                bunxCommand.mapIf({ it == "bunx" }) { "bunx.cmd" }
            } else bunxCommand
            if (download) bunBinDir.dir(command).asFile.absolutePath else command
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

    fun computePnpmBinDir(pnpmDirProvider: Provider<Directory>, platform: Property<Platform>) = computeProductBinDir(pnpmDirProvider, platform)

    fun computePnpmExec(nodeExtension: NodeExtension, pnpmBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.pnpmCommand, nodeExtension.download, pnpmBinDirProvider).map {
            val (pnpmCommand, download, pnpmBinDir) = it
            val command = if (nodeExtension.resolvedPlatform.get().isWindows()) {
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

    fun computeYarnBinDir(yarnDirProvider: Provider<Directory>, platform: Property<Platform>) = computeProductBinDir(yarnDirProvider, platform)

    fun computeYarnExec(nodeExtension: NodeExtension, yarnBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.yarnCommand, yarnBinDirProvider).map {
            val (yarnCommand, yarnBinDir) = it
            val command = if (nodeExtension.resolvedPlatform.get().isWindows()) {
                yarnCommand.mapIf({ it == "yarn" }) { "yarn.cmd" }
            } else yarnCommand
            // This is conceptually pretty simple as we per documentation always download yarn
            yarnBinDir.dir(command).asFile.absolutePath
        }
    }

    fun computeBunDir(nodeExtension: NodeExtension): Provider<Directory> {
        return zip(nodeExtension.bunVersion, nodeExtension.bunWorkDir).map {
            val (bunVersion, bunWorkDir) = it
            val dirnameSuffix = if (bunVersion.isNotBlank()) {
                "-v${bunVersion}"
            } else "-latest"
            val dirname = "bun$dirnameSuffix"
            bunWorkDir.dir(dirname)
        }
    }

    fun computeBunBinDir(bunDirProvider: Provider<Directory>, platform: Property<Platform>) = computeProductBinDir(bunDirProvider, platform)

    fun computeBunExec(nodeExtension: NodeExtension, bunBinDirProvider: Provider<Directory>): Provider<String> {
        return zip(nodeExtension.bunCommand, nodeExtension.download, bunBinDirProvider).map {
            val (bunCommand, download, bunBinDir) = it
            val command = if (nodeExtension.resolvedPlatform.get().isWindows()) {
                bunCommand.mapIf({ it == "bun" }) { "bun.cmd" }
            } else bunCommand
            if (download) bunBinDir.dir(command).asFile.absolutePath else command
        }
    }

    private fun computeProductBinDir(productDirProvider: Provider<Directory>, platform: Property<Platform>) =
            if (platform.get().isWindows()) productDirProvider else productDirProvider.map { it.dir("bin") }

    /**
     * Get the node archive name in Gradle dependency format, using zip for Windows and tar.gz everywhere else.
     *
     * Essentially: org.nodejs:node:$version:$osName-$osArch@tar.gz
     */
    @Deprecated(message = "replaced by package-level function",
        replaceWith = ReplaceWith("com.github.gradle.node.variant.computeNodeArchiveDependency(nodeExtension)"))
    fun computeNodeArchiveDependency(nodeExtension: NodeExtension): Provider<String> {
        return com.github.gradle.node.variant.computeNodeArchiveDependency(nodeExtension)
    }
}
