package com.github.gradle.node.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.ProjectApiHelper
import org.gradle.api.file.DirectoryProperty
import org.gradle.process.ExecResult
import java.io.File

/**
 * Helper function that will calculate the environment variables that should be used.
 *
 * This is operating-system aware and will check Path and PATH in additionalBinPaths on Windows
 *
 * @param execConfiguration configuration to get environment variables from
 */
fun computeEnvironment(execConfiguration: ExecConfiguration): Map<String, String> {
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

fun computeWorkingDir(nodeProjectDir: DirectoryProperty, execConfiguration: ExecConfiguration): File? {
    val workingDir = execConfiguration.workingDir ?: nodeProjectDir.get().asFile
    workingDir.mkdirs()
    return workingDir
}

/**
 * Helper function to find the best matching executable in the system PATH.
 *
 * @param executableName The name of the executable to search for.
 * @return The best matching executable path as a String.
 */
fun findBestExecutableMatch(executableName: String): String {
    val pathVariable = System.getenv("PATH") ?: return executableName
    val paths = pathVariable.split(File.pathSeparator)
    for (path in paths) {
        val executableFile = File(path, executableName)
        if (executableFile.exists() && executableFile.canExecute()) {
            return executableFile.absolutePath
        }
    }
    return executableName  // Return the original executable if no match is found
}

/**
 * Basic execution runner that runs a given ExecConfiguration.
 *
 * Specific implementations likely use the same configuration but may assign
 * different meaning to its values.
 */
class ExecRunner {
    fun execute(projectHelper: ProjectApiHelper, extension: NodeExtension, execConfiguration: ExecConfiguration): ExecResult {
        val executablePath = findBestExecutableMatch(execConfiguration.executable)
        return projectHelper.exec {
            executable = executablePath
            args = execConfiguration.args
            environment = computeEnvironment(execConfiguration)
            isIgnoreExitValue = execConfiguration.ignoreExitValue
            workingDir = computeWorkingDir(extension.nodeProjectDir, execConfiguration)
            execConfiguration.execOverrides?.execute(this)
        }
    }
}
