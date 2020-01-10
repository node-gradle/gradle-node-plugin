package com.moowork.gradle.node.task

import com.moowork.gradle.node.NodePlugin
import com.moowork.gradle.node.exec.NodeExecRunner
import com.moowork.gradle.node.util.MutableAlias
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.process.ExecResult
import java.io.File

open class NodeTask : DefaultTask() {

    @get:Nested
    val execRunner = NodeExecRunner(project)
    @get:Input
    var options = listOf<String>()
    @get:Input
    var args = listOf<String>()
    private var _script: File? = null
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    var script: File?
        get() {
            val currentScript = _script ?: return null
            if (currentScript.isDirectory) {
                // TODO: Fulfill deprecation contract
                logger.warn("Using the NodeTask with a script directory ({}) is deprecated. It will no longer be supported in the next major version.", currentScript)
                return File(currentScript, "index.js")
            }
            return currentScript
        }
        set(value) {
            _script = value
        }

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
        dependsOn(SetupTask.NAME)
    }

    fun setEnvironment(value: Map<String, String>) {
        execRunner.environment.putAll(value)
    }

    @TaskAction
    fun exec() {
        val currentScript = checkNotNull(script) { "Required script property is not set." }
        val execArgs = options.toMutableList()
        execArgs.add(currentScript.absolutePath)
        execArgs.addAll(args)
        execRunner.arguments = execArgs
        result = execRunner.execute()
    }
}
