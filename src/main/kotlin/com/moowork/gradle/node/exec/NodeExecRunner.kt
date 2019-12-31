package com.moowork.gradle.node.exec

import org.gradle.api.Project
import org.gradle.process.ExecResult

class NodeExecRunner(project: Project) : ExecRunner(project) {

    override fun doExecute(): ExecResult {
        val exec = if (ext.download) variant.nodeExec else "node"
        return run(exec, arguments)
    }

    override fun computeAdditionalBinPath(): String {
        return if (ext.download) variant.nodeBinDir.absolutePath else ""
    }
}
