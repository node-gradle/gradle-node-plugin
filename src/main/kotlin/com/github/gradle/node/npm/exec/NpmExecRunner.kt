package com.github.gradle.node.npm.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.proxy.NpmProxy
import com.github.gradle.node.npm.proxy.NpmProxy.Companion.computeNpmProxyEnvironmentVariables
import com.github.gradle.node.services.NodeRuntime
import com.github.gradle.node.util.ProjectApiHelper
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.io.File
import javax.inject.Inject

abstract class NpmExecRunner {
    @get:Inject
    abstract val providers: ProviderFactory

    fun executeNpmCommand(project: ProjectApiHelper, extension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration, variants: VariantComputer) {
        val npmExecConfiguration = NpmExecConfiguration("npm"
        ) { variantComputer, nodeExtension, npmBinDir -> variantComputer.computeNpmExec(nodeExtension, npmBinDir) }
        executeCommand(project, extension, addProxyEnvironmentVariables(extension, nodeExecConfiguration),
            npmExecConfiguration,
            variants)
    }

    fun executeNpmCommand(project: ProjectApiHelper, extension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration, variants: VariantComputer, nodeRuntime: Provider<NodeRuntime>) {
        val npmExecConfiguration = NpmExecConfiguration("npm"
        ) { variantComputer, nodeExtension, npmBinDir -> variantComputer.computeNpmExec(nodeExtension, npmBinDir) }
        executeCommand(project, extension, addProxyEnvironmentVariables(extension, nodeExecConfiguration),
                npmExecConfiguration,
            variants, nodeRuntime)
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

    fun executeNpxCommand(project: ProjectApiHelper, extension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration, variants: VariantComputer) {
        val npxExecConfiguration = NpmExecConfiguration("npx") { variantComputer, nodeExtension, npmBinDir ->
            variantComputer.computeNpxExec(nodeExtension, npmBinDir)
        }
        executeCommand(project, extension, nodeExecConfiguration, npxExecConfiguration, variants)
    }

    private fun executeCommand(project: ProjectApiHelper, extension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration,
                               npmExecConfiguration: NpmExecConfiguration,
                               variantComputer: VariantComputer) {
        val execConfiguration =
                computeExecConfiguration(extension, npmExecConfiguration, nodeExecConfiguration, variantComputer).get()
        val execRunner = ExecRunner()
        execRunner.execute(project, extension, execConfiguration)
    }

    private fun executeCommand(project: ProjectApiHelper, extension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration,
                               npmExecConfiguration: NpmExecConfiguration,
                               variantComputer: VariantComputer, nodeRuntime: Provider<NodeRuntime>) {
        val execConfiguration =
            computeExecConfiguration(extension, npmExecConfiguration, nodeExecConfiguration, variantComputer, nodeRuntime).get()
        val execRunner = ExecRunner()
        execRunner.execute(project, extension, execConfiguration)
    }

    private fun computeExecConfiguration(extension: NodeExtension, npmExecConfiguration: NpmExecConfiguration,
                                         nodeExecConfiguration: NodeExecConfiguration,
                                         variantComputer: VariantComputer): Provider<ExecConfiguration> {
        val additionalBinPathProvider = computeAdditionalBinPath(extension, variantComputer)
        val executableAndScriptProvider = computeExecutable(extension, npmExecConfiguration, variantComputer)
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

    private fun computeExecConfiguration(extension: NodeExtension, npmExecConfiguration: NpmExecConfiguration,
                                         nodeExecConfiguration: NodeExecConfiguration,
                                         variantComputer: VariantComputer,
                                         nodeRuntime: Provider<NodeRuntime> ): Provider<ExecConfiguration> {
        val additionalBinPathProvider = computeAdditionalBinPath(extension, variantComputer)
        val executableAndScriptProvider = computeExecutable(extension, npmExecConfiguration, variantComputer)
        return zip(additionalBinPathProvider, executableAndScriptProvider)
                .map { (additionalBinPath, executableAndScript) ->
                    val argsPrefix = listOf(File(nodeRuntime.get().getNpm(extension).parent, "node_modules/npm/bin/npm-cli.js").absolutePath)
//                            if (executableAndScript.script != null) listOf(executableAndScript.script) else listOf()
                    val args = argsPrefix.plus(nodeExecConfiguration.command)
                    ExecConfiguration(nodeRuntime.get().getNode(extension).absolutePath, args, additionalBinPath,
                            nodeExecConfiguration.environment, nodeExecConfiguration.workingDir,
                            nodeExecConfiguration.ignoreExitValue, nodeExecConfiguration.execOverrides)
                }
    }

    private fun computeExecutable(nodeExtension: NodeExtension, npmExecConfiguration: NpmExecConfiguration, variantComputer: VariantComputer):
            Provider<ExecutableAndScript> {
        val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
        val npmDirProvider = variantComputer.computeNpmDir(nodeExtension, nodeDirProvider)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
        val npmBinDirProvider = variantComputer.computeNpmBinDir(npmDirProvider)
        val nodeExecProvider = variantComputer.computeNodeExec(nodeExtension, nodeBinDirProvider)
        val executableProvider =
                npmExecConfiguration.commandExecComputer(variantComputer, nodeExtension, npmBinDirProvider)
        val npmScriptFileProvider =
                variantComputer.computeNpmScriptFile(nodeDirProvider, npmExecConfiguration.command)
        return zip(nodeExtension.download, nodeExtension.nodeProjectDir, executableProvider, nodeExecProvider,
                npmScriptFileProvider).map {
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

    private fun computeAdditionalBinPath(nodeExtension: NodeExtension, variantComputer: VariantComputer): Provider<List<String>> {
        return nodeExtension.download.flatMap { download ->
            if (!download) {
                providers.provider { listOf<String>() }
            }
            val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
            val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
            val npmDirProvider = variantComputer.computeNpmDir(nodeExtension, nodeDirProvider)
            val npmBinDirProvider = variantComputer.computeNpmBinDir(npmDirProvider)
            zip(npmBinDirProvider, nodeBinDirProvider).map { (npmBinDir, nodeBinDir) ->
                listOf(npmBinDir, nodeBinDir).map { file -> file.asFile.absolutePath }
            }
        }
    }
}
