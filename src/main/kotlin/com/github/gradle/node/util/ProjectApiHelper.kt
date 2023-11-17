@file:Suppress("UnstableApiUsage")

package com.github.gradle.node.util

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.*
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import javax.inject.Inject

@Deprecated(message = "Only 6.6 and newer is supported")
interface ProjectApiHelper {
    companion object {
        @JvmStatic
        fun newInstance(project: Project): ProjectApiHelper {
            return project.objects.newInstance(DefaultProjectApiHelper::class.java)
        }

        const val DEPRECATION_STRING = "ProjectApiHelper is scheduled for removal"
    }

    fun exec(action: Action<ExecSpec>): ExecResult
}

/**
 * Used in Gradle 6.6 and newer.
 */
@Deprecated(message = ProjectApiHelper.DEPRECATION_STRING)
open class DefaultProjectApiHelper @Inject constructor(private val execOperations: ExecOperations) : ProjectApiHelper {

    override fun exec(action: Action<ExecSpec>): ExecResult {
        return execOperations.exec(action)
    }
}

