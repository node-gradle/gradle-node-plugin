package com.github.gradle.node.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.ProjectApiHelper
import org.gradle.api.file.DirectoryProperty
import java.io.File

internal class ExecRunner {
    fun execute(project: ProjectApiHelper, extension: NodeExtension,  execConfiguration: ExecConfiguration) {
        project.exec {
            executable = execConfiguration.executable
            args = execConfiguration.args
            environment = computeEnvironment(execConfiguration)
            isIgnoreExitValue = execConfiguration.ignoreExitValue
            workingDir = computeWorkingDir(extension.nodeProjectDir, execConfiguration)
            execConfiguration.execOverrides?.execute(this)
        }
    }

    private fun computeEnvironment(execConfiguration: ExecConfiguration): Map<String, String> {
        val execEnvironment = mutableMapOf<String, String>()
        execEnvironment += System.getenv()
        execEnvironment += execConfiguration.environment
        if (execConfiguration.additionalBinPaths.isNotEmpty()) {
            // Take care of Windows environments that may contain "Path" OR "PATH" - both existing
            // possibly (but not in parallel as of now)
            val pathEnvironmentVariableName = if (execEnvironment["Path"] != null) "Path" else "PATH"
            val actualPath = execEnvironment[pathEnvironmentVariableName]
            val additionalPathsSerialized = execConfiguration.additionalBinPaths.joinToString(File.pathSeparator)
            execEnvironment[pathEnvironmentVariableName] =
                    "${additionalPathsSerialized}${File.pathSeparator}${actualPath}"
        }
        return execEnvironment
    }

    private fun computeWorkingDir(nodeProjectDir: DirectoryProperty, execConfiguration: ExecConfiguration): File? {
        val workingDir = execConfiguration.workingDir ?: nodeProjectDir.get().asFile
        workingDir.mkdirs()
        return workingDir
    }
}
