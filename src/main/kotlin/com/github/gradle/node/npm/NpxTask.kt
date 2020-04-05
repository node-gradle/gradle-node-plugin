package com.github.gradle.node.npm

import com.github.gradle.node.NodePlugin
import com.github.gradle.node.util.MutableAlias
import com.github.gradle.node.util.OverrideMapAlias
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec

open class NpxTask : DefaultTask() {

    @get:Nested
    val execRunner = NpxExecRunner(project)
    @get:Input
    var args = listOf<String>()
    @get:Input
    var command: String? = null

    @get:Internal
    var result: ExecResult? = null

    @get:Internal
    var ignoreExitValue by MutableAlias { execRunner::ignoreExitValue }
    @get:Internal
    var workingDir by MutableAlias { execRunner::workingDir }
    @get:Internal
    var environment by OverrideMapAlias { execRunner::environment }
    @get:Internal
    var execOverrides by MutableAlias { execRunner::execOverrides }

    init {
        group = NodePlugin.NODE_GROUP
        dependsOn(NpmSetupTask.NAME)
    }

    fun execOverrides(execOverrides: Action<ExecSpec>) {
        execRunner.execOverrides = execOverrides
    }

    @TaskAction
    fun exec() {
        command?.let { execRunner.arguments.add(it) }
        execRunner.arguments.addAll(args)
        result = execRunner.execute()
    }
}
