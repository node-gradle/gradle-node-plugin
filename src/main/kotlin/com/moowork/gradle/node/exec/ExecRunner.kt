package com.moowork.gradle.node.exec

import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.variant.Variant
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import java.io.File
import java.util.*

abstract class ExecRunner(
        @get:Internal protected var project: Project
) {

    @get:Internal
    protected val ext: NodeExtension = NodeExtension[project]
    @get:Internal
    protected val variant: Variant by lazy { ext.variant }

    @get:Input
    val environment = LinkedHashMap<String, String>()
    @get:Input
    var ignoreExitValue = false
    @get:Internal
    var workingDir: File? = null
    @get:Internal
    var arguments: MutableList<String> = ArrayList()
    @get:Internal
    var execOverrides: Action<ExecSpec>? = null

    fun execute(): ExecResult {
        return doExecute()
    }

    protected fun run(exec: String, runArgs: List<String>): ExecResult {
        return project.exec {
            executable = exec
            args = runArgs
            environment = computeExecEnvironment()
            isIgnoreExitValue = ignoreExitValue
            workingDir = computeWorkingDir()
            execOverrides?.invoke(this)
        }
    }

    private fun computeWorkingDir(): File? {
        val computedWorkingDir = workingDir ?: project.extensions.getByType(NodeExtension::class).nodeModulesDir
        computedWorkingDir.mkdirs()
        return computedWorkingDir
    }

    private fun computeExecEnvironment(): Map<String, String> {
        val execEnvironment = linkedMapOf<String, String>()
        execEnvironment += System.getenv()
        execEnvironment += environment
        val path = computeAdditionalBinPath()
        if (path.isNotBlank()) {
            // Take care of Windows environments that may contain "Path" OR "PATH" - both existing
            // possibly (but not in parallel as of now)
            val envPathName = if (execEnvironment["Path"] != null) "Path" else "PATH"
            execEnvironment[envPathName] = path + File.pathSeparator + execEnvironment[envPathName]
        }
        return execEnvironment
    }

    protected abstract fun computeAdditionalBinPath(): String

    protected abstract fun doExecute(): ExecResult
}
