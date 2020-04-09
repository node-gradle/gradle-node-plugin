package com.github.gradle.node.yarn.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import org.gradle.api.Project
import java.io.File

internal class YarnExecRunner {
    fun executeYarnCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val nodeExtension = NodeExtension[project]
        val additionalBinPath = this.computeAdditionalBinPath(nodeExtension)
        val execConfiguration = ExecConfiguration(nodeExtension.variant.yarnExec, nodeExecConfiguration.command,
                additionalBinPath, nodeExecConfiguration.environment, nodeExecConfiguration.workingDir,
                nodeExecConfiguration.ignoreExitValue, nodeExecConfiguration.execOverrides)
        val execRunner = ExecRunner()
        execRunner.execute(project, execConfiguration)
    }

    private fun computeAdditionalBinPath(nodeExtension: NodeExtension): String? {
        if (!nodeExtension.download) {
            return null
        }
        val variant = nodeExtension.variant
        val yarnBinDir = variant.yarnBinDir.absolutePath
        val npmBinDir = variant.npmBinDir.absolutePath
        val nodeBinDir = variant.nodeBinDir.absolutePath
        return yarnBinDir + File.pathSeparator + npmBinDir + File.pathSeparator + nodeBinDir
    }
}
