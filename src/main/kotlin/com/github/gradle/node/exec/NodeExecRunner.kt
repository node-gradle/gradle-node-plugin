package com.github.gradle.node.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

internal class NodeExecRunner {
    fun execute(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val execConfiguration = buildExecConfiguration(project, nodeExecConfiguration).get()
        val execRunner = ExecRunner()
        execRunner.execute(project, execConfiguration)
    }

    private fun buildExecConfiguration(project: Project, nodeExecConfiguration: NodeExecConfiguration):
            Provider<ExecConfiguration> {
        val nodeExtension = NodeExtension[project]
        val variantComputer = VariantComputer()
        val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
        val executableProvider = variantComputer.computeNodeExec(nodeExtension, nodeBinDirProvider)
        val additionalBinPathProvider = computeAdditionalBinPath(nodeExtension, nodeBinDirProvider)
        return zip(executableProvider, additionalBinPathProvider)
                .map { (executable, additionalBinPath) ->
                    ExecConfiguration(executable, nodeExecConfiguration.command, additionalBinPath,
                            nodeExecConfiguration.environment, nodeExecConfiguration.workingDir,
                            nodeExecConfiguration.ignoreExitValue, nodeExecConfiguration.execOverrides)
                }
    }

    private fun computeAdditionalBinPath(nodeExtension: NodeExtension, nodeBinDirProvider: Provider<Directory>):
            Provider<List<String>> {
        return zip(nodeExtension.download, nodeBinDirProvider)
                .map { (download, nodeBinDir) ->
                    if (download) listOf(nodeBinDir.asFile.absolutePath) else listOf()
                }
    }
}
