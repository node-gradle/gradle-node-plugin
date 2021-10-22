package com.github.gradle.node.exec

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.util.ProjectApiHelper
import com.github.gradle.node.util.zip
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

/**
 * This function is responsible for setting up the configuration used when running the tasks.
 */
fun buildExecConfiguration(nodeExtension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration):
        Provider<ExecConfiguration> {
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

fun computeAdditionalBinPath(nodeExtension: NodeExtension, nodeBinDirProvider: Provider<Directory>):
        Provider<List<String>> {
    return zip(nodeExtension.download, nodeBinDirProvider)
        .map { (download, nodeBinDir) ->
            if (download) listOf(nodeBinDir.asFile.absolutePath) else listOf()
        }
}

class NodeExecRunner {
    fun execute(project: ProjectApiHelper, extension: NodeExtension, nodeExecConfiguration: NodeExecConfiguration) {
        val execConfiguration = buildExecConfiguration(extension, nodeExecConfiguration).get()
        val execRunner = ExecRunner()
        execRunner.execute(project, extension, execConfiguration)
    }
}
