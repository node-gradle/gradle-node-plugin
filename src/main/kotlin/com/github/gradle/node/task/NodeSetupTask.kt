package com.github.gradle.node.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.variant.VariantComputer
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

open class NodeSetupTask : DefaultTask() {
    private val variantComputer = VariantComputer()
    private val nodeExtension = NodeExtension[project]

    @get:Input
    val download by lazy { nodeExtension.download }

    @get:Input
    val archiveDependency by lazy {
        variantComputer.computeArchiveDependency(nodeExtension)
    }

    @get:OutputDirectory
    val nodeDir by lazy {
        variantComputer.computeNodeDir(nodeExtension)
    }

    init {
        group = NodePlugin.NODE_GROUP
        description = "Download and install a local node/npm version."
        onlyIf {
            nodeExtension.download.get()
        }
    }

    @TaskAction
    fun exec() {
        addRepositoryIfNeeded()
        val archiveDependency = variantComputer.computeArchiveDependency(nodeExtension).get()
        deleteExistingNode()
        unpackNodeArchive(archiveDependency)
        setExecutableFlag()
    }

    private fun deleteExistingNode() {
        project.delete(nodeDir.get().dir("../"))
    }

    private fun unpackNodeArchive(archiveDependency: String) {
        val nodeArchiveFile = getNodeArchiveFile(archiveDependency)
        val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
        if (nodeArchiveFile.name.endsWith("zip")) {
            project.copy {
                from(project.zipTree(nodeArchiveFile))
                into(nodeDirProvider.map { it.dir("../") })
            }
        } else {
            project.copy {
                from(project.tarTree(nodeArchiveFile))
                into(nodeDirProvider.map { it.dir("../") })
            }
            // Fix broken symlink
            val nodeBinDirPath = nodeBinDirProvider.get().asFile.toPath()
            val npm = nodeBinDirPath.resolve("npm")
            val npmScriptFile = variantComputer.computeNpmScriptFile(nodeDirProvider, "npm").get()
            if (Files.deleteIfExists(npm)) {
                Files.createSymbolicLink(npm, nodeBinDirPath.relativize(Paths.get(npmScriptFile)))
            }
            val npx = nodeBinDirPath.resolve("npx")
            val npxScriptFile = variantComputer.computeNpmScriptFile(nodeDirProvider, "npx").get()
            if (Files.deleteIfExists(npx)) {
                Files.createSymbolicLink(npx, nodeBinDirPath.relativize(Paths.get(npxScriptFile)))
            }
        }
    }

    private fun setExecutableFlag() {
        if (!PlatformHelper.INSTANCE.isWindows) {
            val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
            val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
            val nodeExecProvider = variantComputer.computeNodeExec(nodeExtension, nodeBinDirProvider)
            File(nodeExecProvider.get()).setExecutable(true)
        }
    }

    private fun getNodeArchiveFile(archiveDependency: String): File =
            resolveSingle(archiveDependency)

    private fun resolveSingle(name: String): File {
        val dep = project.dependencies.create(name)
        val conf = project.configurations.detachedConfiguration(dep)
        conf.isTransitive = false
        return conf.resolve().single()
    }

    private fun addRepositoryIfNeeded() {
        nodeExtension.distBaseUrl.orNull?.let { addRepository(it) }
    }

    private fun addRepository(distUrl: String) {
        project.repositories.ivy {
            setUrl(distUrl)
            patternLayout {
                artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]")
                ivy("v[revision]/ivy.xml")
            }
            metadataSources {
                artifact()
            }
        }
    }

    companion object {
        const val NAME = "nodeSetup"
    }
}
