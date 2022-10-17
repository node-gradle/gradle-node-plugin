package com.github.gradle.node.yarn.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.npm.proxy.NpmProxy
import com.github.gradle.node.util.ProjectApiHelper
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecResult
import javax.inject.Inject

abstract class YarnExecRunner {
    @get:Inject
    abstract val providers: ProviderFactory

    fun executeYarnCommand(
        project: ProjectApiHelper,
        nodeExtension: NodeExtension,
        nodeExecConfiguration: NodeExecConfiguration,
        variantComputer: VariantComputer
    ): ExecResult {
        val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
        val yarnDirProvider = variantComputer.computeYarnDir(nodeExtension)
        val yarnBinDirProvider = variantComputer.computeYarnBinDir(yarnDirProvider)
        val yarnExecProvider = variantComputer.computeYarnExec(nodeExtension, yarnBinDirProvider)
        val additionalBinPathProvider =
                computeAdditionalBinPath(nodeExtension, nodeDirProvider, yarnBinDirProvider, variantComputer)
        val execConfiguration = ExecConfiguration(yarnExecProvider.get(),
                nodeExecConfiguration.command, additionalBinPathProvider.get(),
                addNpmProxyEnvironment(nodeExtension, nodeExecConfiguration), nodeExecConfiguration.workingDir,
                nodeExecConfiguration.ignoreExitValue, nodeExecConfiguration.execOverrides)
        val execRunner = ExecRunner()

        return execRunner.execute(project, nodeExtension, execConfiguration)
    }

    private fun addNpmProxyEnvironment(nodeExtension: NodeExtension,
                                       nodeExecConfiguration: NodeExecConfiguration): Map<String, String> {
        if (NpmProxy.shouldConfigureProxy(System.getenv(), nodeExtension.nodeProxySettings.get())) {
            val npmProxyEnvironmentVariables = NpmProxy.computeNpmProxyEnvironmentVariables()
            if (npmProxyEnvironmentVariables.isNotEmpty()) {
                return nodeExecConfiguration.environment.plus(npmProxyEnvironmentVariables)
            }
        }
        return nodeExecConfiguration.environment
    }

    private fun computeAdditionalBinPath(
        nodeExtension: NodeExtension,
        nodeDirProvider: Provider<Directory>,
        yarnBinDirProvider: Provider<Directory>,
        variantComputer: VariantComputer
    ): Provider<List<String>> {
        return nodeExtension.download.flatMap { download ->
            if (!download) {
                providers.provider { listOf<String>() }
            }
            val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
            val npmDirProvider = variantComputer.computeNpmDir(nodeExtension, nodeDirProvider)
            val npmBinDirProvider = variantComputer.computeNpmBinDir(npmDirProvider)
            zip(nodeBinDirProvider, npmBinDirProvider, yarnBinDirProvider)
                    .map { (nodeBinDir, npmBinDir, yarnBinDir) ->
                        listOf(yarnBinDir, npmBinDir, nodeBinDir).map { file -> file.asFile.absolutePath }
                    }
        }
    }
}
