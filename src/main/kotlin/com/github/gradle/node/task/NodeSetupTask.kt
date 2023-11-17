package com.github.gradle.node.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.util.DefaultProjectApiHelper
import com.github.gradle.node.variant.computeNodeExec
import com.github.gradle.node.variant.computeNpmScriptFile
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

abstract class NodeSetupTask : BaseTask() {

    @get:Inject
    abstract val objects: ObjectFactory

    @get:Inject
    abstract val providers: ProviderFactory

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:Inject
    abstract val archiveOperations: ArchiveOperations

    private val nodeExtension = NodeExtension[project]

    @get:Input
    val download = nodeExtension.download

    @get:InputFile
    val nodeArchiveFile = objects.fileProperty()

    @get:OutputDirectory
    abstract val nodeDir: DirectoryProperty

    @get:Internal
    val projectHelper = project.objects.newInstance(DefaultProjectApiHelper::class.java)

    init {
        group = NodePlugin.NODE_GROUP
        description = "Download and install a local node/npm version."
        onlyIf {
            nodeExtension.download.get()
        }
    }

    @TaskAction
    fun exec() {
        deleteExistingNode()
        unpackNodeArchive()
        setExecutableFlag()
    }

    private fun deleteExistingNode() {
        fileSystemOperations.delete {
            delete(nodeDir.get().dir("../"))
        }
    }

    private fun unpackNodeArchive() {
        val archiveFile = nodeArchiveFile.get().asFile
        val nodeDirProvider = nodeExtension.resolvedNodeDir
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider, nodeExtension.resolvedPlatform)
        val archivePath = nodeDirProvider.map { it.dir("../") }
        if (archiveFile.name.endsWith("zip")) {
            fileSystemOperations.copy {
                from(archiveOperations.zipTree(archiveFile))
                into(archivePath)
            }
        } else {
            fileSystemOperations.copy {
                from(archiveOperations.tarTree(archiveFile))
                into(archivePath)
            }
            // Fix broken symlink
            val nodeBinDirPath = nodeBinDirProvider.get().asFile.toPath()
            fixBrokenSymlink("npm", nodeBinDirPath, nodeDirProvider)
            fixBrokenSymlink("npx", nodeBinDirPath, nodeDirProvider)
        }
    }

    private fun fixBrokenSymlink(name: String, nodeBinDirPath: Path, nodeDirProvider: Provider<Directory>) {
        val script = nodeBinDirPath.resolve(name)
        val scriptFile = computeNpmScriptFile(nodeDirProvider, name, nodeExtension.resolvedPlatform.get().isWindows())
        if (Files.deleteIfExists(script)) {
            Files.createSymbolicLink(script, nodeBinDirPath.relativize(Paths.get(scriptFile.get())))
        }
    }

    private fun setExecutableFlag() {
        if (!nodeExtension.resolvedPlatform.get().isWindows()) {
            val nodeBinDirProvider = variantComputer.computeNodeBinDir(
                nodeExtension.resolvedNodeDir,
                nodeExtension.resolvedPlatform
            )
            val nodeExecProvider = computeNodeExec(nodeExtension, nodeBinDirProvider)
            File(nodeExecProvider.get()).setExecutable(true)
        }
    }

    companion object {
        const val NAME = "nodeSetup"
    }
}
