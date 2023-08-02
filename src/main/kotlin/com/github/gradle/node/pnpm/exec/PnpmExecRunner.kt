package com.github.gradle.node.pnpm.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecConfiguration
import com.github.gradle.node.npm.proxy.NpmProxy
import com.github.gradle.node.npm.proxy.NpmProxy.Companion.computeNpmProxyEnvironmentVariables
import com.github.gradle.node.util.ProjectApiHelper
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import com.github.gradle.node.variant.computeNodeExec
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecResult
import javax.inject.Inject

abstract class PnpmExecRunner {
    @get:Inject
    abstract val providers: ProviderFactory

    fun executePnpmCommand(project: ProjectApiHelper, extension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration, variants: VariantComputer): ExecResult {
        val npmExecConfiguration = NpmExecConfiguration("pnpm"
        ) { variantComputer, nodeExtension, pnpmBinDir -> variantComputer.computePnpmExec(nodeExtension, pnpmBinDir) }

        return executeCommand(project, extension, addProxyEnvironmentVariables(extension, nodeExecConfiguration),
            npmExecConfiguration,
            variants)
    }

    private fun addProxyEnvironmentVariables(nodeExtension: NodeExtension,
                                             nodeExecConfiguration: NodeExecConfiguration): NodeExecConfiguration {
        if (NpmProxy.shouldConfigureProxy(System.getenv(), nodeExtension.nodeProxySettings.get())) {
            val npmProxyEnvironmentVariables = computeNpmProxyEnvironmentVariables()
            if (npmProxyEnvironmentVariables.isNotEmpty()) {
                val environmentVariables =
                    nodeExecConfiguration.environment.plus(npmProxyEnvironmentVariables)
                return nodeExecConfiguration.copy(environment = environmentVariables)
            }
        }
        return nodeExecConfiguration
    }

    private fun executeCommand(project: ProjectApiHelper, extension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration,
                               pnpmExecConfiguration: NpmExecConfiguration,
                               variantComputer: VariantComputer): ExecResult {
        val execConfiguration =
            computeExecConfiguration(extension, pnpmExecConfiguration, nodeExecConfiguration, variantComputer).get()
        val execRunner = ExecRunner()

        return execRunner.execute(project, extension, execConfiguration)
    }

    private fun computeExecConfiguration(extension: NodeExtension, pnpmExecConfiguration: NpmExecConfiguration,
                                         nodeExecConfiguration: NodeExecConfiguration,
                                         variantComputer: VariantComputer): Provider<ExecConfiguration> {
        val additionalBinPathProvider = computeAdditionalBinPath(extension, variantComputer)
        val executableAndScriptProvider = computeExecutable(extension, pnpmExecConfiguration, variantComputer)
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
        pnpmExecConfiguration: NpmExecConfiguration,
        variantComputer: VariantComputer
    ):
            Provider<ExecutableAndScript> {
        val nodeDirProvider = nodeExtension.resolvedNodeDir
        val pnpmDirProvider = variantComputer.computePnpmDir(nodeExtension)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider, nodeExtension.resolvedPlatform)
        val pnpmBinDirProvider = variantComputer.computePnpmBinDir(pnpmDirProvider, nodeExtension.resolvedPlatform)
        val nodeExecProvider = computeNodeExec(nodeExtension, nodeBinDirProvider)
        val executableProvider =
            pnpmExecConfiguration.commandExecComputer(variantComputer, nodeExtension, pnpmBinDirProvider)

        return zip(nodeExtension.download, nodeExtension.nodeProjectDir, executableProvider, nodeExecProvider).map {
            val (download, nodeProjectDir, executable, nodeExec) = it
            if (download) {
                val localCommandScript = nodeProjectDir.dir("node_modules/pnpm/bin")
                    .file("${pnpmExecConfiguration.command}.js").asFile
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
            val nodeDirProvider = nodeExtension.resolvedNodeDir
            val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider, nodeExtension.resolvedPlatform)
            val pnpmDirProvider = variantComputer.computePnpmDir(nodeExtension)
            val pnpmBinDirProvider = variantComputer.computePnpmBinDir(pnpmDirProvider, nodeExtension.resolvedPlatform)
            zip(pnpmBinDirProvider, nodeBinDirProvider).map { (pnpmBinDir, nodeBinDir) ->
                listOf(pnpmBinDir, nodeBinDir).map { file -> file.asFile.absolutePath }
            }
        }
    }
}
