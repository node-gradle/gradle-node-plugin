package com.github.gradle.node.yarn

import com.github.gradle.node.exec.ExecRunner
import org.gradle.api.Project
import org.gradle.process.ExecResult
import java.io.File

class YarnExecRunner(project: Project) : ExecRunner(project) {

    override fun doExecute(): ExecResult {
        return run(variant.yarnExec, arguments)
    }

    override fun computeAdditionalBinPath(): String {
        if (!ext.download) {
            return ""
        }
        val yarnBinDir = variant.yarnBinDir.absolutePath
        val npmBinDir = variant.npmBinDir.absolutePath
        val nodeBinDir = variant.nodeBinDir.absolutePath
        return yarnBinDir + File.pathSeparator + npmBinDir + File.pathSeparator + nodeBinDir
    }
}
