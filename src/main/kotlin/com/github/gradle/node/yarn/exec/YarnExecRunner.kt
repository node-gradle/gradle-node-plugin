package com.github.gradle.node.yarn.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.Project
import java.io.File

internal class YarnExecRunner {
    private val variantComputer = VariantComputer()
    fun executeYarnCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val nodeExtension = NodeExtension[project]
        val nodeDir = variantComputer.computeNodeDir(nodeExtension)
        val yarnDir = variantComputer.computeYarnDir(nodeExtension)
        val yarnBinDir = variantComputer.computeYarnBinDir(yarnDir)
        val yarnExec = variantComputer.computeYarnExec(nodeExtension, yarnBinDir)
        val additionalBinPath = computeAdditionalBinPath(nodeExtension, nodeDir, yarnBinDir)
        val execConfiguration = ExecConfiguration(yarnExec, nodeExecConfiguration.command,
                additionalBinPath, nodeExecConfiguration.environment, nodeExecConfiguration.workingDir,
                nodeExecConfiguration.ignoreExitValue, nodeExecConfiguration.execOverrides)
        val execRunner = ExecRunner()
        execRunner.execute(project, execConfiguration)
    }

    private fun computeAdditionalBinPath(nodeExtension: NodeExtension, nodeDir: File, yarnBinDir: File): List<String> {
        if (!nodeExtension.download) {
            return listOf()
        }
        val nodeBinDir = variantComputer.computeNodeBinDir(nodeDir)
        val npmDir = variantComputer.computeNpmDir(nodeExtension, nodeDir)
        val npmBinDir = variantComputer.computeNpmBinDir(npmDir)
        return listOf(yarnBinDir, npmBinDir, nodeBinDir).map { file -> file.absolutePath }
    }
}
