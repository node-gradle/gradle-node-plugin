package com.github.gradle.node.bun.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecConfiguration
import com.github.gradle.node.npm.proxy.NpmProxy
import com.github.gradle.node.util.ProjectApiHelper
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import com.github.gradle.node.variant.computeNodeExec
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecResult
import javax.inject.Inject

abstract class BunExecRunner {
    @get:Inject
    abstract val providers: ProviderFactory

    fun executeBunCommand(project: ProjectApiHelper, extension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration, variants: VariantComputer): ExecResult {
        val bunExecConfiguration = NpmExecConfiguration("bun"
        ) { variantComputer, nodeExtension, binDir -> variantComputer.computeBunExec(nodeExtension, binDir) }

        val enhancedNodeExecConfiguration = NpmProxy.addProxyEnvironmentVariables(extension.nodeProxySettings.get(), nodeExecConfiguration)
        val execConfiguration = computeExecConfiguration(extension, bunExecConfiguration, enhancedNodeExecConfiguration, variants).get()
        return ExecRunner().execute(project, extension, execConfiguration)
    }

    private fun computeExecConfiguration(extension: NodeExtension, bunExecConfiguration: NpmExecConfiguration,
                                         nodeExecConfiguration: NodeExecConfiguration,
                                         variantComputer: VariantComputer): Provider<ExecConfiguration> {
        val additionalBinPathProvider = computeAdditionalBinPath(extension, variantComputer)
        val executableAndScriptProvider = computeExecutable(extension, bunExecConfiguration, variantComputer)
        return zip(additionalBinPathProvider, executableAndScriptProvider)
            .map { (additionalBinPath, executableAndScript) ->
                val argsPrefix =
                    if (executableAndScript.script != null) listOf(executableAndScript.script) else listOf()
                val args = argsPrefix.plus(nodeExecConfiguration.command)
                ExecConfiguration(executableAndScript.executable, args, additionalBinPath,
                    nodeExecConfiguration.environment, nodeExecConfiguration.workingDir,
                    nodeExecConfiguration.ignoreExitValue, nodeExecConfiguration.execOverrides)
            }
    }

    private fun computeExecutable(
        nodeExtension: NodeExtension,
        bunExecConfiguration: NpmExecConfiguration,
        variantComputer: VariantComputer
    ):
            Provider<ExecutableAndScript> {
        val nodeDirProvider = nodeExtension.resolvedNodeDir
        val bunDirProvider = variantComputer.computeBunDir(nodeExtension)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider, nodeExtension.resolvedPlatform)
        val bunBinDirProvider = variantComputer.computeBunBinDir(bunDirProvider, nodeExtension.resolvedPlatform)
        val nodeExecProvider = computeNodeExec(nodeExtension, nodeBinDirProvider)
        val executableProvider =
            bunExecConfiguration.commandExecComputer(variantComputer, nodeExtension, bunBinDirProvider)

        return zip(nodeExtension.download, nodeExtension.nodeProjectDir, executableProvider, nodeExecProvider).map {
            val (download, nodeProjectDir, executable, nodeExec) = it
            if (download) {
                val localCommandScript = nodeProjectDir.dir("node_modules/bun/bin")
                    .file("${bunExecConfiguration.command}.js").asFile
                if (localCommandScript.exists()) {
                    return@map ExecutableAndScript(nodeExec, localCommandScript.absolutePath)
                }
            }
            return@map ExecutableAndScript(executable)
        }
    }

    private data class ExecutableAndScript(
        val executable: String,
        val script: String? = null
    )

    private fun computeAdditionalBinPath(nodeExtension: NodeExtension, variantComputer: VariantComputer): Provider<List<String>> {
        return nodeExtension.download.flatMap { download ->
            if (!download) {
                providers.provider { listOf<String>() }
            }
            val bunDirProvider = variantComputer.computeBunDir(nodeExtension)
            val bunBinDirProvider = variantComputer.computeBunBinDir(bunDirProvider, nodeExtension.resolvedPlatform)
            bunBinDirProvider.map { file -> listOf(file.asFile.absolutePath) }
        }
    }
}
