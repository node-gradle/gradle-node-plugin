package com.moowork.gradle.node.npm

import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.util.MutableAlias
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult

open class NpmTask : DefaultTask() {

    @get:Nested
    val execRunner: NpmExecRunner = NpmExecRunner(project)
    @get:Optional
    @get:Input
    var args = listOf<String>()
    @get:Optional
    @get:Input
    var npmCommand = listOf<String>()

    @get:Internal
    var result: ExecResult? = null

    @get:Internal
    var ignoreExitValue by MutableAlias { execRunner::ignoreExitValue }
    @get:Internal
    var workingDir by MutableAlias { execRunner::workingDir }
    @get:Internal
    var execOverrides by MutableAlias { execRunner::execOverrides }

    init {
        group = NodePlugin.NODE_GROUP
        dependsOn(NpmSetupTask.NAME)
    }

    fun setEnvironment(value: Map<String, String>) {
        execRunner.environment.putAll(value)
    }

    @TaskAction
    fun exec() {
        execRunner.arguments.addAll(npmCommand)
        execRunner.arguments.addAll(args)
        result = execRunner.execute()
    }
}
