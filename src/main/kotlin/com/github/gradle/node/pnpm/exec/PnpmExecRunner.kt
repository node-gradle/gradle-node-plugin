package com.github.gradle.node.pnpm.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.exec.NpmExecConfiguration
import com.github.gradle.node.npm.proxy.NpmProxy
import com.github.gradle.node.util.ProjectApiHelper
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import javax.inject.Inject

// TODO to be factorized with NpmExecRunner
internal abstract class PnpmExecRunner {
    @get:Inject
    abstract val providers: ProviderFactory

    private val variantComputer = VariantComputer()

    fun executePnpmCommand(
        project: ProjectApiHelper,
        nodeExtension: NodeExtension,
        nodeExecConfiguration: NodeExecConfiguration
    ) {
        val pnpmExecConfiguration = NpmExecConfiguration(
            "pnpm"
        ) { variantComputer, pnpmBinDir -> variantComputer.computePnpmExec(nodeExtension, pnpmBinDir) }
        executeCommand(
            project, nodeExtension,
            addProxyEnvironmentVariables(nodeExtension, nodeExecConfiguration),
            pnpmExecConfiguration
        )
    }

    private fun addProxyEnvironmentVariables(
        nodeExtension: NodeExtension,
        nodeExecConfiguration: NodeExecConfiguration
    ): NodeExecConfiguration {
        val environment = NpmProxy.addProxyEnvironmentVariables(nodeExtension, nodeExecConfiguration.environment)
        return nodeExecConfiguration.copy(environment = environment)
    }

    fun executePnpxCommand(
        project: ProjectApiHelper,
        nodeExtension: NodeExtension,
        nodeExecConfiguration: NodeExecConfiguration
    ) {
        val pnpxExecConfiguration = NpmExecConfiguration(
            "pnpx"
        ) { variantComputer, pnpmBinDir -> variantComputer.computePnpxExec(nodeExtension, pnpmBinDir) }
        executeCommand(project, nodeExtension, nodeExecConfiguration, pnpxExecConfiguration)
    }

    private fun executeCommand(
        project: ProjectApiHelper, nodeExtension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration,
        pnpmExecConfiguration: NpmExecConfiguration
    ) {
        val execConfiguration =
            computeExecConfiguration(nodeExtension, pnpmExecConfiguration, nodeExecConfiguration).get()
        val execRunner = ExecRunner()
        execRunner.execute(project, nodeExtension, execConfiguration)
    }

    private fun computeExecConfiguration(
        nodeExtension: NodeExtension, pnpmExecConfiguration: NpmExecConfiguration,
        nodeExecConfiguration: NodeExecConfiguration
    ): Provider<ExecConfiguration> {
        val additionalBinPathProvider = computeAdditionalBinPath(nodeExtension)
        val executableAndScriptProvider =
            computeExecutable(nodeExtension, pnpmExecConfiguration)
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

    private fun computeExecutable(nodeExtension: NodeExtension, pnpmExecConfiguration: NpmExecConfiguration):
            Provider<ExecutableAndScript> {
        val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
        val pnpmDirProvider = variantComputer.computePnpmDir(nodeExtension)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
        val pnpmBinDirProvider = variantComputer.computePnpmBinDir(pnpmDirProvider)
        val nodeExecProvider = variantComputer.computeNodeExec(nodeExtension, nodeBinDirProvider)
        val executableProvider =
            pnpmExecConfiguration.commandExecComputer(variantComputer, pnpmBinDirProvider)

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

    private fun computeAdditionalBinPath(nodeExtension: NodeExtension): Provider<List<String>> {
        return nodeExtension.download.flatMap { download ->
            if (!download) {
                providers.provider { listOf<String>() }
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
