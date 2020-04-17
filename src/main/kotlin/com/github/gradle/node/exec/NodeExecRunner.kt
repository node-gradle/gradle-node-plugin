package com.github.gradle.node.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.Project
import java.io.File

internal class NodeExecRunner {
    fun execute(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val execConfiguration = buildExecConfiguration(project, nodeExecConfiguration)
        val execRunner = ExecRunner()
        execRunner.execute(project, execConfiguration)
    }

    private fun buildExecConfiguration(project: Project, nodeExecConfiguration: NodeExecConfiguration): ExecConfiguration {
        val nodeExtension = NodeExtension[project]
        val variantComputer = VariantComputer()
        val nodeDir = variantComputer.computeNodeDir(nodeExtension)
        val nodeBinDir = variantComputer.computeNodeBinDir(nodeDir)
        val executable = variantComputer.computeNodeExec(nodeExtension, nodeBinDir)
        val additionalBinPath = computeAdditionalBinPath(nodeExtension, nodeBinDir)
        return ExecConfiguration(executable, nodeExecConfiguration.command, additionalBinPath,
                nodeExecConfiguration.environment, nodeExecConfiguration.workingDir,
                nodeExecConfiguration.ignoreExitValue, nodeExecConfiguration.execOverrides)
    }

    private fun computeAdditionalBinPath(nodeExtension: NodeExtension, nodeBinDir: File) =
            if (nodeExtension.download) listOf(nodeBinDir.absolutePath) else listOf()
}
