package com.github.gradle.node.exec

import com.github.gradle.node.NodeExtension
import org.gradle.api.Project

internal class NodeExecRunner {
    fun execute(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val execConfiguration = buildExecConfiguration(project, nodeExecConfiguration)
        val execRunner = ExecRunner()
        execRunner.execute(project, execConfiguration)
    }

    private fun buildExecConfiguration(project: Project, nodeExecConfiguration: NodeExecConfiguration): ExecConfiguration {
        val nodeExtension = NodeExtension[project]
        val variant = nodeExtension.variant
        val executable = if (nodeExtension.download) variant.nodeExec else "node"
        val additionalBinPath = if (nodeExtension.download) variant.nodeBinDir.absolutePath else null
        return ExecConfiguration(executable, nodeExecConfiguration.command, additionalBinPath,
                nodeExecConfiguration.environment, nodeExecConfiguration.workingDir,
                nodeExecConfiguration.ignoreExitValue, nodeExecConfiguration.execOverrides)
    }
}
