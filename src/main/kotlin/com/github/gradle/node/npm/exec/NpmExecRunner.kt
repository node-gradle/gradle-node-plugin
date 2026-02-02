package com.github.gradle.node.npm.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.proxy.NpmProxy
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import com.github.gradle.node.variant.computeNodeExec
import com.github.gradle.node.variant.computeNpmScriptFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import java.io.File
import javax.inject.Inject

abstract class NpmExecRunner {
    @get:Inject
    abstract val providers: ProviderFactory

    fun executeNpmCommand(
        execOperations: ExecOperations,
        extension: NodeExtension,
        nodeExecConfiguration: NodeExecConfiguration,
        variants: VariantComputer
    ): ExecResult {
        val npmExecConfiguration = NpmExecConfiguration(
            "npm"
        ) { variantComputer, nodeExtension, npmBinDir -> variantComputer.computeNpmExec(nodeExtension, npmBinDir) }
        return executeCommand(
            execOperations,
            extension,
            NpmProxy.addProxyEnvironmentVariables(extension.nodeProxySettings.get(), nodeExecConfiguration),
            npmExecConfiguration,
            variants
        )
    }

    fun executeNpxCommand(
        execOperations: ExecOperations,
        extension: NodeExtension,
        nodeExecConfiguration: NodeExecConfiguration,
        variants: VariantComputer
    ): ExecResult {
        val npxExecConfiguration = NpmExecConfiguration("npx") { variantComputer, nodeExtension, npmBinDir ->
            variantComputer.computeNpxExec(nodeExtension, npmBinDir)
        }

        return executeCommand(execOperations, extension, nodeExecConfiguration, npxExecConfiguration, variants)
    }

    private fun executeCommand(
        execOperations: ExecOperations,
        extension: NodeExtension,
        nodeExecConfiguration: NodeExecConfiguration,
        npmExecConfiguration: NpmExecConfiguration,
        variantComputer: VariantComputer
    ): ExecResult {
        val execConfiguration =
            computeExecConfiguration(extension, npmExecConfiguration, nodeExecConfiguration, variantComputer).get()
        val execRunner = ExecRunner()
        return execRunner.execute(execOperations, extension, execConfiguration)
    }

    private fun computeExecConfiguration(
        extension: NodeExtension,
        npmExecConfiguration: NpmExecConfiguration,
        nodeExecConfiguration: NodeExecConfiguration,
        variantComputer: VariantComputer
    ): Provider<ExecConfiguration> {
        val additionalBinPathProvider = computeAdditionalBinPath(extension, variantComputer)
        val executableAndScriptProvider = computeExecutable(extension, npmExecConfiguration, variantComputer)
        return zip(additionalBinPathProvider, executableAndScriptProvider)
            .map { (additionalBinPath, executableAndScript) ->
                val argsPrefix =
                    if (executableAndScript.script != null) listOf(executableAndScript.script) else listOf()
                val args = argsPrefix.plus(nodeExecConfiguration.command)
                ExecConfiguration(
                    executableAndScript.executable, args, additionalBinPath,
                    nodeExecConfiguration.environment, nodeExecConfiguration.workingDir,
                    nodeExecConfiguration.ignoreExitValue, nodeExecConfiguration.execOverrides
                )
            }
    }

    private fun computeExecutable(
        nodeExtension: NodeExtension,
        npmExecConfiguration: NpmExecConfiguration,
        variantComputer: VariantComputer
    ): Provider<ExecutableAndScript> {
        val nodeDirProvider = nodeExtension.resolvedNodeDir
        val npmDirProvider = variantComputer.computeNpmDir(nodeExtension, nodeDirProvider)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider, nodeExtension.resolvedPlatform)
        val npmBinDirProvider = variantComputer.computeNpmBinDir(npmDirProvider, nodeExtension.resolvedPlatform)
        val nodeExecProvider = computeNodeExec(nodeExtension, nodeBinDirProvider)
        val executableProvider =
            npmExecConfiguration.commandExecComputer(variantComputer, nodeExtension, npmBinDirProvider)
        val isWindows = nodeExtension.resolvedPlatform.get().isWindows()
        val npmScriptFileProvider =
            computeNpmScriptFile(nodeDirProvider, npmExecConfiguration.command, isWindows)
        return zip(
            nodeExtension.download, nodeExtension.nodeProjectDir, executableProvider, nodeExecProvider,
            npmScriptFileProvider
        ).map {
            val (download, nodeProjectDir, executable, nodeExec,
                npmScriptFile) = it
            if (download) {
                val localCommandScript = nodeProjectDir.dir("node_modules/npm/bin")
                    .file("${npmExecConfiguration.command}-cli.js").asFile
                if (localCommandScript.exists()) {
                    return@map ExecutableAndScript(nodeExec, localCommandScript.absolutePath)
                } else if (!File(executable).exists()) {
                    return@map ExecutableAndScript(nodeExec, npmScriptFile)
                }
            }
            return@map ExecutableAndScript(executable)
        }
    }

    private data class ExecutableAndScript(
        val executable: String,
        val script: String? = null
    )

    private fun computeAdditionalBinPath(
        nodeExtension: NodeExtension,
        variantComputer: VariantComputer
    ): Provider<List<String>> {
        return nodeExtension.download.flatMap { download ->
            if (!download) {
                providers.provider { listOf<String>() }
            }
            val nodeDirProvider = nodeExtension.resolvedNodeDir
            val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider, nodeExtension.resolvedPlatform)
            val npmDirProvider = variantComputer.computeNpmDir(nodeExtension, nodeDirProvider)
            val npmBinDirProvider = variantComputer.computeNpmBinDir(npmDirProvider, nodeExtension.resolvedPlatform)
            zip(npmBinDirProvider, nodeBinDirProvider).map { (npmBinDir, nodeBinDir) ->
                listOf(npmBinDir, nodeBinDir).map { file -> file.asFile.absolutePath }
            }
        }
    }
}
