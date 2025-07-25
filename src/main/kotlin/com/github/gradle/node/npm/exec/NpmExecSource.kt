// TODO remove suppress when updating Gradle to a version where ValueSource is stable
@file:Suppress("UnstableApiUsage")

package com.github.gradle.node.npm.exec

import com.github.gradle.node.npm.proxy.NpmProxy
import com.github.gradle.node.util.Platform
import com.github.gradle.node.util.mapIf
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

/**
 * Runs `npm` using a [ValueSource].
 *
 * All options can be configured using [NpmExecSource.Parameters],
 * but this is a delicate API and may cause issues.
 * Prefer using [NpmExecSpec].
 */
abstract class NpmExecSource @Inject internal constructor(
    private val execOps: ExecOperations,
) : ValueSource<NpmExecResult, NpmExecSource.Parameters> {

    abstract class Parameters internal constructor() : NpmExecSpec(), ValueSourceParameters {
        abstract val ignoreExitValue: Property<Boolean>
        abstract val workingDir: DirectoryProperty
        abstract val coreNpmCommand: Property<String>
        abstract val npmCommand: ListProperty<String>
    }


    private val coreNpmCommand get() = parameters.coreNpmCommand.get()
    private val execNpmCommand get() = parameters.npmCommand.get()
    private val nodeProxySettings get() = parameters.nodeProxySettings.get()
    private val npmVersion get() = parameters.npmVersion.get()
    private val npmWorkDir get() = parameters.npmWorkDir.get()

    private val download: Boolean get() = parameters.download.getOrElse(true)
    private val platform get() = parameters.resolvedPlatform.get()
    private val nodeProjectDir get() = parameters.nodeProjectDir.get()

    private val resolvedPlatform get() = parameters.resolvedPlatform.get()
    private val resolvedNodeDir get() = parameters.resolvedNodeDir.get()


    override fun obtain(): NpmExecResult {
        val command = execNpmCommand.plus(parameters.arguments.get())
        val nodeExecConfiguration =
            NodeExecConfiguration(
                command = command,
                environment = parameters.environment.orNull.orEmpty(),
                workingDir = parameters.workingDir.asFile.orNull,
            )
        return executeNpmCommand(nodeExecConfiguration)
    }

    private fun computeEnvironment(
        execConfiguration: ExecConfiguration
    ): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            if (parameters.includeSystemEnvironment.getOrElse(true)) {
                putAll(System.getenv())
            }
            putAll(parameters.environment.get())
            val additionalBinPaths = execConfiguration.additionalBinPaths
            if (additionalBinPaths.isNotEmpty()) {
                // Take care of Windows environments that may contain "Path" OR "PATH" - both existing
                // possibly (but not in parallel as of now)
                val pathEnvironmentVariableName = if (get("Path") != null) "Path" else "PATH"
                val originalPath = get(pathEnvironmentVariableName)
                val newPath = (additionalBinPaths + originalPath).filterNotNull().joinToString(File.pathSeparator)
                put(pathEnvironmentVariableName, newPath)
            }
        }
    }


    private fun executeNpmCommand(
        nodeExecConfiguration: NodeExecConfiguration,
    ): NpmExecResult {
        val npmExecConfiguration = NpmExecConfiguration(
            command = "npm",
            commandExecComputer = { npmBinDir ->
                computeNpmExec(
                    npmBinDir
                )
            }
        )

        val proxiedEnvVars = NpmProxy.createProxyEnvironmentVariables(
            proxySettings = nodeProxySettings,
            nodeExecConfigurationEnvironment = nodeExecConfiguration.environment
        )

        val proxiedNodeExecConfiguration = nodeExecConfiguration.copy(environment = proxiedEnvVars)

        return executeCommand(
            nodeExecConfiguration = proxiedNodeExecConfiguration,
            npmExecConfiguration = npmExecConfiguration,
        )
    }

    private fun executeCommand(
        nodeExecConfiguration: NodeExecConfiguration,
        npmExecConfiguration: NpmExecConfiguration,
    ): NpmExecResult {
        val execConfiguration =
            computeExecConfiguration(npmExecConfiguration, nodeExecConfiguration)

        ByteArrayOutputStream().use { capturedOutput ->
            val result = execOps.exec {

                executable = execConfiguration.executable
                args = execConfiguration.args
                environment = computeEnvironment(execConfiguration)
                workingDir = computeWorkingDir(nodeProjectDir, execConfiguration)
                isIgnoreExitValue = parameters.ignoreExitValue.getOrElse(true)
                standardOutput = capturedOutput
                errorOutput = capturedOutput
            }

            val failure = try {
                result.rethrowFailure()
                null
            } catch (ex: GradleException) {
                ex
            }

            return NpmExecResult(
                exitValue = result.exitValue,
                failure = failure,
                capturedOutput = capturedOutput.toString(),
            )
        }
    }

    private fun computeExecConfiguration(
        npmExecConfiguration: NpmExecConfiguration,
        nodeExecConfiguration: NodeExecConfiguration,
    ): ExecConfiguration {
        val additionalBinPath = computeAdditionalBinPath()
        val executableAndScript = computeExecutable(npmExecConfiguration)
        val argsPrefix =
            if (executableAndScript.script != null) listOf(executableAndScript.script) else listOf()
        val args = argsPrefix.plus(nodeExecConfiguration.command)
        return ExecConfiguration(
            executable = executableAndScript.executable,
            args = args,
            additionalBinPaths = additionalBinPath,
            environment = nodeExecConfiguration.environment,
            workingDir = nodeExecConfiguration.workingDir,
            execOverrides = nodeExecConfiguration.execOverrides,
        )
    }

    private fun computeExecutable(
        npmExecConfiguration: NpmExecConfiguration,
    ): ExecutableAndScript {
        val nodeDirProvider = parameters.resolvedNodeDir.get()
        val npmDirProvider = computeNpmDir(nodeDirProvider)
        val nodeBinDirProvider = computeNodeBinDir(nodeDirProvider, parameters.resolvedPlatform.get())
        val npmBinDirProvider = computeNpmBinDir(npmDirProvider, parameters.resolvedPlatform.get())
        val nodeExecProvider = computeNodeExec(nodeBinDirProvider)
        val executableProvider =
            computeNpmExec(npmBinDirProvider)
        val npmScriptFileProvider =
            computeNpmScriptFile(nodeDirProvider, npmExecConfiguration.command, resolvedPlatform)
        return computeExecutable(
            npmExecConfiguration.command,
            executableProvider,
            nodeExecProvider,
            npmScriptFileProvider
        )
    }

    private fun computeExecutable(
        command: String,
        executable: String,
        nodeExec: String,
        npmScriptFile: String,
    ): ExecutableAndScript {
        if (download) {
            val localCommandScript = nodeProjectDir.dir("node_modules/npm/bin")
                .file("${command}-cli.js").asFile
            if (localCommandScript.exists()) {
                return ExecutableAndScript(nodeExec, localCommandScript.absolutePath)
            } else if (!File(executable).exists()) {
                return ExecutableAndScript(nodeExec, npmScriptFile)
            }
        }
        return ExecutableAndScript(executable)
    }

    private data class ExecutableAndScript(
        val executable: String,
        val script: String? = null,
    )

    private fun computeAdditionalBinPath(): List<String> {
        if (!download) {
            return emptyList()
        }
        val nodeDirProvider = resolvedNodeDir
        val nodeBinDirProvider = computeNodeBinDir(nodeDirProvider, resolvedPlatform)
        val npmDirProvider = computeNpmDir(nodeDirProvider)
        val npmBinDirProvider = computeNpmBinDir(npmDirProvider, resolvedPlatform)
        return listOf(npmBinDirProvider, nodeBinDirProvider).map { file -> file.asFile.absolutePath }
    }


    /**
     * Get the expected node binary directory, taking Windows specifics into account.
     */
    private fun computeNodeBinDir(
        nodeDirProvider: Directory,
        platform: Platform,
    ): Directory =
        computeProductBinDir(nodeDirProvider, platform)

    /**
     * Get the expected node binary name, node.exe on Windows and node everywhere else.
     */
    private fun computeNodeExec(
        nodeBinDir: Directory
    ): String {
        return if (download) {
            val nodeCommand = if (platform.isWindows()) "node.exe" else "node"
            nodeBinDir.dir(nodeCommand).asFile.absolutePath
        } else {
            "node"
        }
    }

    /**
     * Get the expected directory for a given npm version.
     */
    private fun computeNpmDir(
        nodeDir: Directory,
    ): Directory {
        return if (npmVersion.isNotBlank()) {
            val directoryName = "npm-v${npmVersion}"
            npmWorkDir.dir(directoryName)
        } else {
            nodeDir
        }
    }

    /**
     * Get the expected npm binary directory, taking Windows specifics into account.
     */
    private fun computeNpmBinDir(
        npmDirProvider: Directory,
        platform: Platform
    ): Directory =
        computeProductBinDir(npmDirProvider, platform)

    /**
     * Get the expected node binary name, `npm.cmd` on Windows and `npm` everywhere else.
     *
     * Can be overridden by setting `npmCommand`.
     */
    private fun computeNpmExec(npmBinDirProvider: Directory): String {
        return computeExec(
            binDirProvider = npmBinDirProvider,
            configurationCommand = coreNpmCommand,
        )
    }

    /**
     * Compute the path for a given command, from a given binary directory, taking Windows into account
     */
    private fun computeExec(
        binDirProvider: Directory,
        configurationCommand: String,
        unixCommand: String = "npm",
        windowsCommand: String = "npm.cmd",
    ): String {
        val command = if (resolvedPlatform.isWindows()) {
            configurationCommand.mapIf({ it == unixCommand }) { windowsCommand }
        } else {
            configurationCommand
        }
        return if (download) {
            binDirProvider.dir(command).asFile.absolutePath
        } else {
            command
        }
    }

    private fun computeProductBinDir(
        productDirProvider: Directory,
        platform: Platform
    ): Directory {
        return if (platform.isWindows()) {
            productDirProvider.dir("bin")
        } else {
            productDirProvider
        }
    }


    private data class ExecConfiguration(
        val executable: String,
        val args: List<String> = listOf(),
        val additionalBinPaths: List<String> = listOf(),
        val environment: Map<String, String> = mapOf(),
        val workingDir: File? = null,
        val execOverrides: Action<ExecSpec>? = null
    )

    private data class NpmExecConfiguration(
        val command: String,
        val commandExecComputer: (
            npmBinDir: Directory,
        ) -> String,
    )

    private fun computeWorkingDir(
        nodeProjectDir: Directory,
        execConfiguration: ExecConfiguration
    ): File? {
        val workingDir = execConfiguration.workingDir ?: nodeProjectDir.asFile
        workingDir.mkdirs()
        return workingDir
    }

    private fun computeNpmScriptFile(
        nodeDir: Directory,
        command: String,
        platform: Platform,
    ): String {
        return if (platform.isWindows()) {
            nodeDir.dir("node_modules/npm/bin/$command-cli.js").asFile.path
        } else {
            nodeDir.dir("lib/node_modules/npm/bin/$command-cli.js").asFile.path
        }
    }

    private data class NodeExecConfiguration(
        val command: List<String> = listOf(),
        val environment: Map<String, String> = mapOf(),
        val workingDir: File? = null,
        val execOverrides: Action<ExecSpec>? = null
    )
}
