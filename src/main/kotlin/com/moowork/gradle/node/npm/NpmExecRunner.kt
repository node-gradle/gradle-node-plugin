package com.moowork.gradle.node.npm

import com.moowork.gradle.node.exec.ExecRunner
import org.gradle.api.Project
import org.gradle.process.ExecResult
import java.io.File

open class NpmExecRunner(project: Project) : ExecRunner(project) {

    override fun doExecute(): ExecResult {
        var exec = getCommand()
        val arguments = arguments.toMutableList()
        if (ext.download) {
            val localNpm = getLocalCommandScript()
            if (localNpm.exists()) {
                exec = variant.nodeExec
                arguments.add(0, localNpm.absolutePath)
            } else if (!File(exec).exists()) {
                exec = variant.nodeExec
                arguments.add(0, getCommandScript())
            }
        }
        return run(exec, arguments)
    }

    override fun computeAdditionalBinPath(): String {
        if (!ext.download) {
            return ""
        }
        val npmBinDir = variant.npmBinDir.absolutePath
        val nodeBinDir = variant.nodeBinDir.absolutePath
        return npmBinDir + File.pathSeparator + nodeBinDir
    }

    protected open fun getCommand(): String = this.variant.npmExec
    protected open fun getLocalCommandScript(): File = this.project.file(File(ext.nodeModulesDir, "node_modules/npm/bin/npm-cli.js"))
    protected open fun getCommandScript(): String = this.variant.npmScriptFile
}
