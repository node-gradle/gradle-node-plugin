package com.moowork.gradle.node.npm

import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import java.io.File

class NpxExecRunner(project: Project) : NpmExecRunner(project) {

    @Internal
    override fun getCommand(): String {
        return variant.npxExec
    }

    @Internal
    override fun getLocalCommandScript(): File {
        return project.file(File(ext.nodeModulesDir, "node_modules/npm/bin/npx-cli.js"))
    }

    @Internal
    override fun getCommandScript(): String {
        return variant.npxScriptFile
    }
}
