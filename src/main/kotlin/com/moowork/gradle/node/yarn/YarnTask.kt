package com.moowork.gradle.node.yarn

import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.util.MutableAlias
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult

open class YarnTask : DefaultTask() {

    @get:Nested
    val execRunner = YarnExecRunner(project)
    @get:Optional
    @get:Input
    var args = listOf<String>()
    @get:Optional
    @get:Input
    var yarnCommand = listOf<String>()

    @get:Internal
    var result: ExecResult? = null

    @get:Internal
    var execOverrides by MutableAlias { execRunner::execOverrides }
    @get:Internal
    var ignoreExitValue by MutableAlias { execRunner::ignoreExitValue }
    @get:Internal
    var workingDir by MutableAlias { execRunner::workingDir }

    init {
        group = NodePlugin.NODE_GROUP
        dependsOn(YarnSetupTask.NAME)
    }

    fun setEnvironment(value: Map<String, String>) {
        execRunner.environment.putAll(value)
    }

    @TaskAction
    fun exec() {
        execRunner.arguments.addAll(yarnCommand)
        execRunner.arguments.addAll(args)
        result = execRunner.execute()
    }
}
