@file:Suppress("UnstableApiUsage")

package com.github.gradle.node.util

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.util.GradleVersion
import java.io.File
import javax.inject.Inject


abstract class ProjectApiHelper {
    companion object {
        @JvmStatic
        fun newInstance(project: Project): ProjectApiHelper {
            return if (enableConfigurationCache()) {
                project.objects.newInstance(DefaultProjectApiHelper::class.java)
            } else {
                LegacyProjectApiHelper(project)
            }
        }

        inline fun enableConfigurationCache(): Boolean {
            return GradleVersion.current() >= GradleVersion.version("6.6")
        }

    }
    abstract fun fileTree(directory: Directory?): ConfigurableFileTree?

    abstract fun exec(closure: Action<ExecSpec?>?): ExecResult?
}

internal open class DefaultProjectApiHelper @Inject constructor(
        private val factory: ObjectFactory,
        private val layout: ProjectLayout,
        private val execOperations: ExecOperations) : ProjectApiHelper() {

    val buildDirectory: File
        get() = layout.buildDirectory.get().asFile

    fun file(path: String?): File? {
        return if (path != null) {
            layout.projectDirectory.file(path).asFile
        } else {
            null
        }
    }

    fun file(file: File): File? {
        return file(file.path)
    }

    override fun fileTree(directory: Directory?): ConfigurableFileTree? {
        return directory?.let { factory.fileTree().from(it) }
    }

    override fun exec(closure: Action<ExecSpec?>?): ExecResult {
        return execOperations.exec(closure)
    }

}

internal open class LegacyProjectApiHelper(private val project: Project) : ProjectApiHelper() {
    val buildDirectory: File
        get() = project.buildDir

    fun file(path: String?): File? {
        return path?.let { project.file(it) }
    }

    fun file(file: File?): File? {
        return if (file != null) {
            project.file(file)
        } else {
            null
        }
    }

    override fun fileTree(directory: Directory?): ConfigurableFileTree? {
        return directory?.let { project.fileTree(it) }
    }

    override fun exec(closure: Action<ExecSpec?>?): ExecResult? {
        return closure?.let { project.exec(it) }
    }
}
