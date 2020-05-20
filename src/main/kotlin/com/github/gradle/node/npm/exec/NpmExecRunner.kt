package com.github.gradle.node.npm.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.proxy.NpmProxy.Companion.computeNpmProxyCliArgs
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.File

internal class NpmExecRunner {
    private val variantComputer = VariantComputer()

    fun executeNpmCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val npmExecConfiguration = NpmExecConfiguration("npm"
        ) { variantComputer, nodeExtension, npmBinDir -> variantComputer.computeNpmExec(nodeExtension, npmBinDir) }
        val nodeExtension = NodeExtension[project]
        executeCommand(project, addProxyCliArgs(nodeExtension, nodeExecConfiguration), npmExecConfiguration)
    }

    private fun addProxyCliArgs(nodeExtension: NodeExtension,
                                nodeExecConfiguration: NodeExecConfiguration): NodeExecConfiguration {
        if (nodeExtension.useGradleProxySettings.get()) {
            val npmProxyCliArgs = computeNpmProxyCliArgs()
            if (npmProxyCliArgs.isNotEmpty()) {
                val commandWithProxy = npmProxyCliArgs.plus(nodeExecConfiguration.command)
                return nodeExecConfiguration.copy(command = commandWithProxy)
            }
        }
        return nodeExecConfiguration
    }

    fun executeNpxCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val npxExecConfiguration = NpmExecConfiguration("npx"
        ) { variantComputer, nodeExtension, npmBinDir -> variantComputer.computeNpxExec(nodeExtension, npmBinDir) }
        executeCommand(project, nodeExecConfiguration, npxExecConfiguration)
    }

    private fun executeCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration,
                               npmExecConfiguration: NpmExecConfiguration) {
        val execConfiguration =
                computeExecConfiguration(project, npmExecConfiguration, nodeExecConfiguration).get()
        val execRunner = ExecRunner()
        execRunner.execute(project, execConfiguration)
    }

    private fun computeExecConfiguration(project: Project, npmExecConfiguration: NpmExecConfiguration,
                                         nodeExecConfiguration: NodeExecConfiguration): Provider<ExecConfiguration> {
        val nodeExtension = NodeExtension[project]
        val additionalBinPathProvider = computeAdditionalBinPath(project, nodeExtension)
        val executableAndScriptProvider =
                computeExecutable(nodeExtension, npmExecConfiguration)
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

    private fun computeExecutable(nodeExtension: NodeExtension, npmExecConfiguration: NpmExecConfiguration):
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

    private fun computeAdditionalBinPath(project: Project, nodeExtension: NodeExtension): Provider<List<String>> {
        return nodeExtension.download.flatMap { download ->
            if (!download) {
                project.providers.provider { listOf<String>() }
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
