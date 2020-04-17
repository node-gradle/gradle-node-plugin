package com.github.gradle.node.npm.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.exec.ExecConfiguration
import com.github.gradle.node.exec.ExecRunner
import com.github.gradle.node.exec.NodeExecConfiguration
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.Project
import java.io.File

internal class NpmExecRunner {
    private val variantComputer = VariantComputer()

    fun executeNpmCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val npmExecConfiguration = NpmExecConfiguration("npm"
        ) { variantComputer, nodeExtension, npmBinDir -> variantComputer.computeNpmExec(nodeExtension, npmBinDir) }
        executeCommand(project, nodeExecConfiguration, npmExecConfiguration)
    }

    fun executeNpxCommand(project: Project, nodeExecConfiguration: NodeExecConfiguration) {
        val npxExecConfiguration = NpmExecConfiguration("npx"
        ) { variantComputer, nodeExtension, npmBinDir -> variantComputer.computeNpxExec(nodeExtension, npmBinDir) }
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
        val nodeDir = variantComputer.computeNodeDir(nodeExtension)
        val npmDir = variantComputer.computeNpmDir(nodeExtension, nodeDir)
        val nodeBinDir = variantComputer.computeNodeBinDir(nodeDir)
        val npmBinDir = variantComputer.computeNpmBinDir(npmDir)
        val nodeExec = variantComputer.computeNodeExec(nodeExtension, nodeBinDir)
        val executable = npmExecConfiguration.commandExecComputer(variantComputer, nodeExtension, npmBinDir)
        val npmScriptFile = variantComputer.computeNpmScriptFile(nodeDir, npmExecConfiguration.command)
        if (nodeExtension.download) {
            val localCommandScript = nodeExtension.nodeModulesDir.toPath()
                    .resolve("node_modules").resolve("npm").resolve("bin")
                    .resolve("${npmExecConfiguration.command}-cli.js").toFile()
            if (localCommandScript.exists()) {
                return ExecutableAndScript(nodeExec, localCommandScript.absolutePath)
            } else if (!File(executable).exists()) {
                return ExecutableAndScript(nodeExec, npmScriptFile)
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
        val nodeDir = variantComputer.computeNodeDir(nodeExtension)
        val nodeBinDir = variantComputer.computeNodeBinDir(nodeDir)
        val npmDir = variantComputer.computeNpmDir(nodeExtension, nodeDir)
        val npmBinDir = variantComputer.computeNpmBinDir(npmDir)
        return npmBinDir.absolutePath + File.pathSeparator + nodeBinDir.absolutePath
    }
}
