package com.github.gradle.node.pnpm.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecConfiguration
import com.github.gradle.node.npm.proxy.NpmProxy.Companion.computeNpmProxyCliArgs
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.Project
import org.gradle.api.provider.Provider

internal class PnpmExecRunner {
    private val variantComputer = VariantComputer()

    fun executePnpmCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val pnpmExecConfiguration = NpmExecConfiguration("pnpm"
        ) { variantComputer, nodeExtension, pnpmBinDir -> variantComputer.computePnpmExec(nodeExtension, pnpmBinDir) }
        executeCommand(project, addProxyCliArgs(nodeExecConfiguration), pnpmExecConfiguration)
    }

    private fun addProxyCliArgs(nodeExecConfiguration: NodeExecConfiguration): NodeExecConfiguration {
        val pnpmProxyCliArgs = computeNpmProxyCliArgs()
        if (pnpmProxyCliArgs.isNotEmpty()) {
            val commandWithProxy = pnpmProxyCliArgs.plus(nodeExecConfiguration.command)
            return nodeExecConfiguration.copy(command = commandWithProxy)
        }
        return nodeExecConfiguration
    }

    fun executePnpxCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val pnpxExecConfiguration = NpmExecConfiguration("pnpx"
        ) { variantComputer, nodeExtension, pnpmBinDir -> variantComputer.computePnpxExec(nodeExtension, pnpmBinDir) }
        executeCommand(project, nodeExecConfiguration, pnpxExecConfiguration)
    }

    private fun executeCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration,
                               pnpmExecConfiguration: NpmExecConfiguration) {
        val execConfiguration =
                computeExecConfiguration(project, pnpmExecConfiguration, nodeExecConfiguration).get()
        val execRunner = ExecRunner()
        execRunner.execute(project, execConfiguration)
    }

    private fun computeExecConfiguration(project: Project, pnpmExecConfiguration: NpmExecConfiguration,
                                         nodeExecConfiguration: NodeExecConfiguration): Provider<ExecConfiguration> {
        val nodeExtension = NodeExtension[project]
        val additionalBinPathProvider = computeAdditionalBinPath(project, nodeExtension)
        val executableAndScriptProvider =
                computeExecutable(nodeExtension, pnpmExecConfiguration)
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

    private fun computeExecutable(nodeExtension: NodeExtension, pnpmExecConfiguration: NpmExecConfiguration):
            Provider<ExecutableAndScript> {
        val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
        val pnpmDirProvider = variantComputer.computePnpmDir(nodeExtension)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
        val pnpmBinDirProvider = variantComputer.computePnpmBinDir(pnpmDirProvider)
        val nodeExecProvider = variantComputer.computeNodeExec(nodeExtension, nodeBinDirProvider)
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

    private fun computeAdditionalBinPath(project: Project, nodeExtension: NodeExtension): Provider<List<String>> {
        return nodeExtension.download.flatMap { download ->
            if (!download) {
                project.providers.provider { listOf<String>() }
            }
            val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
            val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
            val pnpmDirProvider = variantComputer.computePnpmDir(nodeExtension)
            val pnpmBinDirProvider = variantComputer.computePnpmBinDir(pnpmDirProvider)
            zip(pnpmBinDirProvider, nodeBinDirProvider).map { (pnpmBinDir, nodeBinDir) ->
                listOf(pnpmBinDir, nodeBinDir).map { file -> file.asFile.absolutePath }
            }
        }
    }
}
