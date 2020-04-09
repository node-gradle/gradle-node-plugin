package com.github.gradle.node.npm.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import org.gradle.api.Project
import java.io.File

internal class NpmExecRunner {
    fun executeNpmCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val nodeExtension = NodeExtension[project]
        val npmExecConfiguration = NpmExecConfiguration(nodeExtension.variant.npmExec, "npm-cli.js",
                nodeExtension.variant.npmScriptFile)
        executeCommand(project, nodeExecConfiguration, npmExecConfiguration)
    }

    fun executeNpxCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val nodeExtension = NodeExtension[project]
        val npxExecConfiguration = NpmExecConfiguration(nodeExtension.variant.npxExec, "npx-cli.js",
                nodeExtension.variant.npxScriptFile)
        executeCommand(project, nodeExecConfiguration, npxExecConfiguration)
    }

    private fun executeCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration,
                               npmExecConfiguration: NpmExecConfiguration) {
        val nodeExtension = NodeExtension[project]
        val additionalBinPath = computeAdditionalBinPath(nodeExtension)
        val executableAndScript = computeExecutable(nodeExtension, npmExecConfiguration)
        val argsPrefix = if (executableAndScript.script != null) listOf(executableAndScript.script) else listOf()
        val args = argsPrefix.plus(nodeExecConfiguration.command)
        val execConfiguration = ExecConfiguration(executableAndScript.executable, args, additionalBinPath,
                nodeExecConfiguration.environment, nodeExecConfiguration.workingDir,
                nodeExecConfiguration.ignoreExitValue, nodeExecConfiguration.execOverrides)
        val execRunner = ExecRunner()
        execRunner.execute(project, execConfiguration)
    }

    private fun computeExecutable(nodeExtension: NodeExtension, npmExecConfiguration: NpmExecConfiguration): ExecutableAndScript {
        val executable = npmExecConfiguration.command
        if (nodeExtension.download) {
            val localCommandScript = nodeExtension.nodeModulesDir.toPath()
                    .resolve("node_modules").resolve("npm").resolve("bin")
                    .resolve(npmExecConfiguration.localCommandScript).toFile()
            if (localCommandScript.exists()) {
                return ExecutableAndScript(nodeExtension.variant.nodeExec, localCommandScript.absolutePath)
            } else if (!File(executable).exists()) {
                return ExecutableAndScript(nodeExtension.variant.nodeExec, npmExecConfiguration.commandScript)
            }
        }
        return ExecutableAndScript(executable)
    }

    private data class ExecutableAndScript(
            val executable: String,
            val script: String? = null
    )

    private fun computeAdditionalBinPath(nodeExtension: NodeExtension): String? {
        if (!nodeExtension.download) {
            return null
        }
        val variant = nodeExtension.variant
        val npmBinDir = variant.npmBinDir.absolutePath
        val nodeBinDir = variant.nodeBinDir.absolutePath
        return npmBinDir + File.pathSeparator + nodeBinDir
    }
}
