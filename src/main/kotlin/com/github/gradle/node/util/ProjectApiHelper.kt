@file:Suppress("UnstableApiUsage")

package com.github.gradle.node.util

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.*
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.WorkResult
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.util.GradleVersion
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@Deprecated(message = "Only 7.5.1 and newer is supported")
interface ProjectApiHelper {
    companion object {
        @JvmStatic
        fun newInstance(project: Project): ProjectApiHelper {
            return project.objects.newInstance(DefaultProjectApiHelper::class.java, )
        }
    }

    fun fileTree(directory: Directory): ConfigurableFileTree

    fun zipTree(tarPath: File): FileTree

    fun tarTree(tarPath: File): FileTree

    fun copy(action: Action<CopySpec>): WorkResult

    fun delete(action: Action<DeleteSpec>): WorkResult

    fun exec(action: Action<ExecSpec>): ExecResult
}

internal class GradleHelperExecution(private val eo: ExecOperations) : HelperExecution {
    override fun exec(command: String, vararg args: String, timeout: Long): String {
        val out = ByteArrayOutputStream()
        val cmd = eo.exec {
            this.executable = command
            this.args = args.toList()
            this.standardOutput = out
        }

        cmd.assertNormalExitValue()
        return out.toString().trim()
    }
}


/**
 * Used in Gradle 6.6 and newer.
 */
open class DefaultProjectApiHelper @Inject constructor(
        private val factory: ObjectFactory,
        private val execOperations: ExecOperations,
        private val fileSystemOperations: FileSystemOperations,
        private val archiveOperations: ArchiveOperations) : ProjectApiHelper {

    override fun fileTree(directory: Directory): ConfigurableFileTree {
        return factory.fileTree().from(directory)
    }

    override fun zipTree(tarPath: File): FileTree {
        return archiveOperations.zipTree(tarPath)
    }

    override fun tarTree(tarPath: File): FileTree {
        return archiveOperations.tarTree(tarPath)
    }

    override fun copy(action: Action<CopySpec>): WorkResult {
        return fileSystemOperations.copy(action)
    }

    override fun delete(action: Action<DeleteSpec>): WorkResult {
        return fileSystemOperations.delete(action)
    }

    override fun exec(action: Action<ExecSpec>): ExecResult {
        return execOperations.exec(action)
    }
}

/**
 * Used to support Gradle versions older than 6.6
 */
open class LegacyProjectApiHelper(private val project: Project) : ProjectApiHelper {

    override fun fileTree(directory: Directory): ConfigurableFileTree {
        return project.fileTree(directory)
    }

    override fun zipTree(tarPath: File): FileTree {
        return project.zipTree(tarPath)
    }

    override fun tarTree(tarPath: File): FileTree {
        return project.tarTree(tarPath)
    }

    override fun copy(action: Action<CopySpec>): WorkResult {
        return project.copy(action)
    }

    override fun delete(action: Action<DeleteSpec>): WorkResult {
        return project.delete(action)
    }

    override fun exec(action: Action<ExecSpec>): ExecResult {
        return project.exec(action)
    }
}
