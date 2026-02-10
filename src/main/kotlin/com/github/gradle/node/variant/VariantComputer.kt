package com.github.gradle.node.variant

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.Platform
import com.github.gradle.node.util.mapIf
import com.github.gradle.node.util.zip
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

/**
 * Get the expected node binary name, node.exe on Windows and node everywhere else.
 */
fun computeNodeExec(
    nodeExtension: NodeExtension,
    nodeBinDirProvider: Provider<Directory>
): Provider<String> {
    return zip(
        nodeExtension.download,
        nodeBinDirProvider,
        nodeExtension.resolvedPlatform,
    ).map { (download, nodeBinDir, platform) ->
        if (download) {
            val nodeCommand = if (platform.isWindows()) "node.exe" else "node"
            nodeBinDir.dir(nodeCommand).asFile.absolutePath
        } else {
            "node"
        }
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
 * Compute the path for a given command, from a given binary directory, taking Windows into account
 */
internal fun computeExec(
    nodeExtension: NodeExtension,
    binDirProvider: Provider<Directory>,
    configurationCommand: Property<String>,
    unixCommand: String,
    windowsCommand: String,
): Provider<String> {
    return zip(
        nodeExtension.download,
        nodeExtension.resolvedPlatform,
        configurationCommand,
        binDirProvider,
    ).map { (download, resolvedPlatform, cfgCommand, binDir) ->
        val command = if (resolvedPlatform.isWindows()) {
            cfgCommand.mapIf({ it == unixCommand }) { windowsCommand }
        } else {
            cfgCommand
        }
        if (download) {
            binDir.dir(command).asFile.absolutePath
        } else {
            command
        }
    }
}

/**
 * Compute the path for a given package, taken versions and user-configured working directories into account
 */
internal fun computePackageDir(
    packageName: String,
    packageVersion: Property<String>,
    packageWorkDir: DirectoryProperty
): Provider<Directory> {
    return zip(packageVersion, packageWorkDir).map {
        val (version, workDir) = it
        val dirnameSuffix = if (version.isNotBlank()) {
            "-v${version}"
        } else "-latest"
        val dirname = "$packageName$dirnameSuffix"
        workDir.dir(dirname)
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
    fun computeNodeBinDir(
        nodeDirProvider: Provider<Directory>,
        platform: Property<Platform>,
    ): Provider<Directory> =
        computeProductBinDir(nodeDirProvider, platform)

    /**
     * Get the expected node binary name, node.exe on Windows and node everywhere else.
     */
    @Deprecated(
        message = "replaced by package-level function",
        replaceWith =
            ReplaceWith("com.github.gradle.node.variant.computeNodeExec(nodeExtension, nodeBinDirProvider)")
    )
    fun computeNodeExec(nodeExtension: NodeExtension, nodeBinDirProvider: Provider<Directory>): Provider<String> {
        return com.github.gradle.node.variant.computeNodeExec(nodeExtension, nodeBinDirProvider)
    }

    /**
     * Get the expected directory for a given npm version.
     */
    fun computeNpmDir(
        nodeExtension: NodeExtension,
        nodeDirProvider: Provider<Directory>,
    ): Provider<Directory> {
        return zip(nodeExtension.npmVersion, nodeExtension.npmWorkDir, nodeDirProvider)
            .map { (npmVersion, npmWorkDir, nodeDir) ->
                if (npmVersion.isNotBlank()) {
                    val directoryName = "npm-v${npmVersion}"
                    npmWorkDir.dir(directoryName)
                } else {
                    nodeDir
                }
            }
    }

    /**
     * Get the expected npm binary directory, taking Windows specifics into account.
     */
    fun computeNpmBinDir(
        npmDirProvider: Provider<Directory>,
        platform: Property<Platform>
    ): Provider<Directory> =
        computeProductBinDir(npmDirProvider, platform)

    /**
     * Get the expected node binary name, npm.cmd on Windows and npm everywhere else.
     *
     * Can be overridden by setting npmCommand.
     */
    fun computeNpmExec(nodeExtension: NodeExtension, npmBinDirProvider: Provider<Directory>): Provider<String> {
        return computeExec(
            nodeExtension = nodeExtension,
            binDirProvider = npmBinDirProvider,
            configurationCommand = nodeExtension.npmCommand,
            unixCommand = "npm",
            windowsCommand = "npm.cmd",
        )
    }

    /**
     * Get the expected node binary name, npx.cmd on Windows and npx everywhere else.
     *
     * Can be overridden by setting npxCommand.
     */
    fun computeNpxExec(nodeExtension: NodeExtension, npmBinDirProvider: Provider<Directory>): Provider<String> {
        return computeExec(
            nodeExtension, npmBinDirProvider,
            nodeExtension.npxCommand, "npx", "npx.cmd"
        )
    }

    fun computePnpmDir(nodeExtension: NodeExtension): Provider<Directory> {
        return computePackageDir("pnpm", nodeExtension.pnpmVersion, nodeExtension.pnpmWorkDir)
    }

    fun computePnpmBinDir(pnpmDirProvider: Provider<Directory>, platform: Property<Platform>) =
        computeProductBinDir(pnpmDirProvider, platform)

    fun computePnpmExec(nodeExtension: NodeExtension, pnpmBinDirProvider: Provider<Directory>): Provider<String> {
        return computeExec(
            nodeExtension, pnpmBinDirProvider,
            nodeExtension.pnpmCommand, "pnpm", "pnpm.cmd"
        )
    }

    fun computeYarnDir(nodeExtension: NodeExtension): Provider<Directory> {
        return computePackageDir("yarn", nodeExtension.yarnVersion, nodeExtension.yarnWorkDir)
    }

    fun computeYarnBinDir(yarnDirProvider: Provider<Directory>, platform: Property<Platform>) =
        computeProductBinDir(yarnDirProvider, platform)

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
        return computePackageDir("bun", nodeExtension.bunVersion, nodeExtension.bunWorkDir)
    }

    fun computeBunBinDir(bunDirProvider: Provider<Directory>, platform: Property<Platform>) =
        computeProductBinDir(bunDirProvider, platform)

    fun computeBunExec(nodeExtension: NodeExtension, bunBinDirProvider: Provider<Directory>): Provider<String> {
        return computeExec(
            nodeExtension, bunBinDirProvider,
            nodeExtension.bunCommand, "bun", "bun.cmd"
        )
    }

    /**
     * Get the expected bunx binary name, bunx.cmd on Windows and bunx everywhere else.
     *
     * Can be overridden by setting bunxCommand.
     */
    fun computeBunxExec(nodeExtension: NodeExtension, bunBinDirProvider: Provider<Directory>): Provider<String> {
        return computeExec(
            nodeExtension, bunBinDirProvider,
            nodeExtension.bunxCommand, "bunx", "bunx.cmd"
        )
    }

    private fun computeProductBinDir(
        productDirProvider: Provider<Directory>,
        platform: Property<Platform>
    ): Provider<Directory> {
        return platform.flatMap { p ->
            if (p.isWindows()) {
                productDirProvider.map { it.dir("bin") }
            } else {
                productDirProvider
            }
        }
    }

    /**
     * Get the node archive name in Gradle dependency format, using zip for Windows and tar.gz everywhere else.
     *
     * Essentially: org.nodejs:node:$version:$osName-$osArch@tar.gz
     */
    @Deprecated(
        message = "replaced by package-level function",
        replaceWith = ReplaceWith("com.github.gradle.node.variant.computeNodeArchiveDependency(nodeExtension)")
    )
    fun computeNodeArchiveDependency(nodeExtension: NodeExtension): Provider<String> {
        return com.github.gradle.node.variant.computeNodeArchiveDependency(nodeExtension)
    }
}
