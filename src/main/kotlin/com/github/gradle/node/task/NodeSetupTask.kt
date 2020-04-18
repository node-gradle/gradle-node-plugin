package com.github.gradle.node.task

import com.github.gradle.node.NodeExtension
import com.github.gradle.node.NodePlugin
import com.github.gradle.node.util.PlatformHelper
import com.github.gradle.node.variant.VariantComputer
import com.github.gradle.node.variant.VariantComputer.Dependency
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
    val dependency by lazy {
        variantComputer.computeDependency(nodeExtension)
                .map { Pair(it.archiveDependency, it.exeDependency) }
    }

    @get:OutputDirectory
    val nodeDir by lazy {
        variantComputer.computeNodeDir(nodeExtension)
    }

    init {
        group = NodePlugin.NODE_GROUP
        description = "Download and install a local node/npm version."
        isEnabled = false
        project.afterEvaluate {
            isEnabled = nodeExtension.download.get()
        }
    }

    @TaskAction
    fun exec() {
        addRepositoryIfNeeded()
        val dependency = variantComputer.computeDependency(nodeExtension).get()
        if (!dependency.exeDependency.isNullOrBlank()) {
            copyNodeExe(dependency)
        }
        deleteExistingNode()
        unpackNodeArchive(dependency)
        setExecutableFlag()
    }

    private fun copyNodeExe(dependency: Dependency) {
        val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
        project.copy {
            from(getNodeExeFile(dependency))
            into(nodeBinDirProvider)
            rename("node.*\\.exe", "node.exe")
        }
    }

    private fun deleteExistingNode() {
        project.delete(nodeDir.get().dir("../"))
    }

    private fun unpackNodeArchive(dependency: Dependency) {
        val nodeArchiveFile = getNodeArchiveFile(dependency)
        val nodeDirProvider = variantComputer.computeNodeDir(nodeExtension)
        val nodeBinDirProvider = variantComputer.computeNodeBinDir(nodeDirProvider)
        if (nodeArchiveFile.name.endsWith("zip")) {
            project.copy {
                from(project.zipTree(nodeArchiveFile))
                into(nodeDirProvider.map { it.dir("../") })
            }
        } else if (!dependency.exeDependency.isNullOrBlank()) {
            // Remap lib/node_modules to node_modules (the same directory as node.exe) because that's how the zip dist does it
            project.copy {
                from(project.tarTree(nodeArchiveFile))
                into(nodeBinDirProvider)
                val nodeModulesPattern = Regex("""^.*?[\\/]lib[\\/](node_modules.*$)""")
                eachFile {
                    val file = this
                    val matchResult = nodeModulesPattern.matchEntire(file.path)
                    if (matchResult != null) {
                        // Remap the file to the root
                        file.path = matchResult.groupValues[1]
                    } else {
                        file.exclude()
                    }
                }
                includeEmptyDirs = false
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

    private fun getNodeExeFile(dependency: Dependency): File? =
            dependency.exeDependency?.let { resolveSingle(it) }

    private fun getNodeArchiveFile(dependency: Dependency): File =
            resolveSingle(dependency.archiveDependency)

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
