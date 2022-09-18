package com.github.gradle.node.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.util.ProjectApiHelper
import org.gradle.api.file.Directory
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

    private val nodeExtension = NodeExtension[project]

    @get:Input
    val download = nodeExtension.download

    @get:InputFile
    val nodeArchiveFile = objects.fileProperty()

    @get:OutputDirectory
    val nodeDir by lazy {
        variantComputer.computeNodeDir(nodeExtension)
    }

    @get:Internal
    val projectHelper = ProjectApiHelper.newInstance(project)

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
        projectHelper.delete {
            delete(nodeDir.get().dir("../"))
        }
    }

    private fun unpackNodeArchive() {
        val archiveFile = nodeArchiveFile.get().asFile
        val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
        val archivePath = nodeDirProvider.map { it.dir("../") }
        if (archiveFile.name.endsWith("zip")) {
            projectHelper.copy {
                from(projectHelper.zipTree(archiveFile))
                into(archivePath)
            }
        } else {
            projectHelper.copy {
                from(projectHelper.tarTree(archiveFile))
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
        val scriptFile = variantComputer.computeNpmScriptFile(nodeDirProvider, name).get()
        if (Files.deleteIfExists(script)) {
            Files.createSymbolicLink(script, nodeBinDirPath.relativize(Paths.get(scriptFile)))
        }
    }

    private fun setExecutableFlag() {
        if (!platformHelper.isWindows) {
            val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
            val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
            val nodeExecProvider = variantComputer.computeNodeExec(nodeExtension, nodeBinDirProvider)
            File(nodeExecProvider.get()).setExecutable(true)
        }
    }

    companion object {
        const val NAME = "nodeSetup"
    }
}
